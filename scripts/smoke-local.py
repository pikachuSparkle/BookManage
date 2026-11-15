from pathlib import Path
from html.parser import HTMLParser
from http.cookiejar import CookieJar
from urllib.request import build_opener, HTTPCookieProcessor, Request, HTTPRedirectHandler
from urllib.parse import urlencode
from urllib.error import HTTPError
from concurrent.futures import ThreadPoolExecutor
import os, re, uuid

ROOT = Path(__file__).resolve().parents[1]
BASE = os.environ.get('SMOKE_BASE', 'http://127.0.0.1:8080')
if os.environ.get('ALLOW_SMOKE_WRITES') != '1':
    raise SystemExit('This test creates a book and a historical borrow. Run only on a demo database with ALLOW_SMOKE_WRITES=1.')
PASSWORD = os.environ.get('SMOKE_PASSWORD') or re.search(r'^Password: (.+)$', (ROOT/'.runtime/credentials.txt').read_text(), re.M)[1]
checks = []

class NoRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None

class Page(HTMLParser):
    def __init__(self, text):
        super().__init__(); self.inputs = []; self.rows = []; self.row = None; self.cell = None
        self.feed(text)
    def handle_starttag(self, tag, attrs):
        attrs = dict(attrs)
        if tag == 'input': self.inputs.append(attrs)
        if tag == 'tr': self.row = []
        if tag == 'td': self.cell = ''
    def handle_data(self, text):
        if self.cell is not None: self.cell += text
    def handle_endtag(self, tag):
        if tag == 'td' and self.row is not None:
            self.row.append(self.cell.strip()); self.cell = None
        if tag == 'tr' and self.row:
            self.rows.append(self.row); self.row = None
    def csrf(self):
        return next(i['value'] for i in self.inputs if i.get('name') == '_csrf')

def session():
    return build_opener(HTTPCookieProcessor(CookieJar()), NoRedirect())

def request(client, path, data=None, csrf=None):
    if data is not None:
        data = dict(data)
        if csrf: data['_csrf'] = csrf
    req = Request(BASE+path, data=None if data is None else urlencode(data).encode('utf-8'))
    try: response = client.open(req, timeout=20)
    except HTTPError as error: response = error
    return response.code, response.read().decode('utf-8'), response.headers

def expect(status, expected, label):
    assert status == expected, (label, status, expected)
    checks.append(label)

def login():
    client = session()
    status, text, _ = request(client, '/login'); expect(status, 200, 'login-page')
    status, _, _ = request(client, '/doLogin', {'username':'admin','password':PASSWORD,'remember-me':'on'}, Page(text).csrf())
    expect(status, 302, 'bcrypt-login')
    return client

def main():
    client = login()
    for path in ['/borrow','/books','/student','/students','/add-book','/add-borrow','/static/css/style.css']:
        status, text, _ = request(client, path); expect(status, 200, 'page:'+path)
        if not path.startswith('/static/'):
            assert ' th:' not in text
    status, _, _ = request(client, '/add-book', {'title':'无令牌','desc':'test','price':'1'})
    expect(status, 403, 'missing-csrf-rejected')
    _, text, _ = request(client, '/add-book'); token = Page(text).csrf()
    status, _, _ = request(client, '/add-book', {'title':'','desc':'test','price':'-1'}, token)
    expect(status, 400, 'invalid-book-rejected')
    title = '联调验证图书-中文-' + uuid.uuid4().hex[:8]
    status, _, _ = request(client, '/add-book', {'title':title,'desc':'人民币精度验证','price':'12.34'}, token)
    expect(status, 302, 'create-book')
    _, text, _ = request(client, '/books'); page = Page(text)
    row = next(row for row in page.rows if row[1] == title)
    bid = int(row[0].lstrip('#')); assert '12.34' in row[3]
    checks.extend(['utf8-form-data','decimal-cents'])
    expect(request(client, f'/delete-book?bid={bid}')[0], 405, 'get-delete-rejected')
    _, text, _ = request(client, '/add-borrow'); token = Page(text).csrf()
    expect(request(client, '/add-borrow', {'student':999999,'book':bid}, token)[0], 409, 'missing-student-rejected')
    other = login()
    _, text2, _ = request(other, '/add-borrow'); token2 = Page(text2).csrf()
    with ThreadPoolExecutor(max_workers=2) as pool:
        first = pool.submit(request, client, '/add-borrow', {'student':1,'book':bid}, token)
        second = pool.submit(request, other, '/add-borrow', {'student':2,'book':bid}, token2)
        assert sorted([first.result()[0],second.result()[0]]) == [302,409]
    checks.append('real-mariadb-concurrent-borrow-single-winner')
    _, text, _ = request(client, '/borrow'); page = Page(text)
    row = next(row for row in page.rows if row[1] == title)
    borrow_id = int(row[0].lstrip('#'))
    token = page.csrf()
    expect(request(client, '/delete-book', {'bid':bid}, token)[0],409,'borrowed-book-delete-rejected')
    expect(request(client, '/return-book', {'id':borrow_id}, token)[0],302,'return-book')
    expect(request(client, '/return-book', {'id':borrow_id}, token)[0],409,'double-return-rejected')
    expect(request(client, '/delete-book', {'bid':bid}, token)[0],302,'delete-returned-book')
    expect(request(client, '/doLogout', {})[0],403,'logout-csrf-required')
    expect(request(client, '/doLogout', {}, token)[0],302,'logout')
    expect(request(client, '/books')[0],302,'logout-clears-authentication')
    output = '\n'.join('PASS '+name for name in checks) + f'\nTest book ID: {bid}; borrow ID: {borrow_id}\n'
    (ROOT/'.runtime/smoke-results.txt').write_text(output)
    print(output)

if __name__ == '__main__': main()
