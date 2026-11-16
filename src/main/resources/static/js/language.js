/* Shared bilingual presentation. Database values and form submissions stay intact. */
(() => {
    'use strict';
    const pairs = [
        ['图书管理系统', 'Library Management System'],
        ['欢迎来到图书管理系统', 'Welcome to the Library Management System'],
        ['退出登录', 'Sign out'], ['借阅管理', 'Loan Management'],
        ['书籍管理', 'Book Management'], ['学生列表', 'Students'],
        ['学号', 'Student ID'], ['姓名', 'Name'], ['性别', 'Gender'], ['年级', 'Year'],
        ['网页模板', 'Website Template'],
        ['Copyright © 2021.Company name All rights reserved.', 'Copyright © 2021.Company name All rights reserved.'],
        ['输入书籍标题...', 'Search by book title...'], ['添加书籍信息', 'Add Book'],
        ['书籍列表', 'Books'], ['书籍ID', 'Book ID'], ['书籍标题', 'Book Title'],
        ['书籍简介', 'Description'], ['书籍价格', 'Price'], ['借阅状态', 'Loan Status'],
        ['删除信息', 'Delete'], ['正在借阅', 'On Loan'], ['可借阅', 'Available'],
        ['删除图书', 'Delete Book'], ['添加借阅信息', 'Add Loan'], ['添加借阅', 'New Loan'],
        ['请在下方选择所借阅的', 'Select a'], ['书籍', 'Book'],
        ['以及对应的', 'and its'], ['借阅人', 'Borrower'],
        ['当前没有可借阅图书，请先归还或添加图书。', 'No books available. Return or add a book first.'],
        ['信息选择', 'Loan Details'], ['学生', 'Student'], ['提交', 'Submit'],
        ['录入书籍信息', 'New Book'], ['请输入书名', 'Enter a book title'],
        ['请输入简介', 'Enter a description'], ['价格', 'Price'],
        ['请填写书籍标题、简介和价格。', 'Enter the book title, description, and price.'],
        ['操作未完成', 'Operation Failed'], ['请检查输入后重试', 'Check your input and try again'],
        ['图书列表', 'Book List'], ['借阅列表', 'Loan List'],
        ['图书借阅管理', 'Library Loan Management'], ['学生数量', 'Total Students'],
        ['书籍数量', 'Total Books'], ['借阅数量', 'Active Loans'], ['可借书籍数量', 'Available Books'],
        ['图书借阅列表', 'Book Loans'], ['借阅ID', 'Loan ID'], ['书籍名称', 'Book Name'],
        ['借阅时间', 'Borrowed At'], ['借阅人学号', 'Borrower ID'], ['归还', 'Return'],
        ['归还图书', 'Return Book'], ['我是书名', 'Book Title'], ['我是学生', 'Student Name'],
        ['用户名或密码错误，重新输入！', 'Incorrect username or password. Please try again.'],
        ['你好，', 'Hello,'], ['欢迎！', 'Welcome!'],
        ['请输入用户名和密码进行登录', 'Enter your username and password to sign in'],
        ['用户名', 'Username'], ['密码', 'Password'], ['记住我', 'Remember me'], ['登录', 'Sign in'],
        ['出错了！', 'Duhh!'], ['发生错误', 'Something Went Wrong'],
        ['书名不能为空', 'Book title is required'], ['书名最多255字', 'Book title must be at most 255 characters'],
        ['简介不能为空', 'Description is required'], ['简介最多255字', 'Description must be at most 255 characters'],
        ['请输入价格', 'Price is required'], ['价格不能为负数', 'Price cannot be negative'],
        ['价格最多8位整数和2位小数', 'Price allows up to 8 integer digits and 2 decimal places'],
        ['输入格式不正确', 'Invalid input format'], ['请求参数缺失或格式不正确', 'Missing or invalid request parameters'],
        ['数据库操作失败，请稍后重试', 'Database operation failed. Please try again later.'],
        ['用户名或密码错误', 'Incorrect username or password'], ['学生不存在', 'Student not found'],
        ['该图书已被借阅，请刷新页面', 'This book is already on loan. Please refresh the page.'],
        ['借阅记录不存在', 'Loan not found'], ['该图书已经归还', 'This book has already been returned'],
        ['图书正在借阅中，不能删除', 'A book on loan cannot be deleted'],
        ['图书不存在或已删除', 'Book not found or already deleted'],
        ['小明', 'James Miller'], ['小红', 'Emily Davis'], ['张晨', 'Michael Chen'],
        ['李雨桐', 'Sophia Li'], ['王浩然', 'Daniel Wang'], ['陈思涵', 'Olivia Zhang'], ['赵子轩', 'Ethan Zhao'],
        ['男', 'Male'], ['女', 'Female'],
        ['Java 编程入门', 'Introduction to Java Programming'], ['Spring 实战', 'Spring in Action'],
        ['三体', 'The Three-Body Problem'], ['活着', 'To Live'], ['平凡的世界', 'Ordinary World'],
        ['围城', 'Fortress Besieged'], ['红楼梦', 'Dream of the Red Chamber'],
        ['百年孤独', 'One Hundred Years of Solitude'], ['人类简史', 'Sapiens: A Brief History of Humankind'],
        ['算法图解', 'Grokking Algorithms'], ['深入理解计算机系统', "Computer Systems: A Programmer's Perspective"],
        ['设计模式', 'Design Patterns'],
        ['Java 基础与面向对象编程', 'Java fundamentals and object-oriented programming'],
        ['Spring MVC、安全与数据访问', 'Spring MVC, security, and data access'],
        ['刘慈欣科幻小说，讲述地球文明与三体文明的接触。', 'A science fiction novel by Liu Cixin about contact between Earth and the Trisolarans.'],
        ['余华长篇小说，描写普通人在时代变迁中的生命历程。', 'A novel by Yu Hua depicting the life of an ordinary person through changing times.'],
        ['路遥长篇小说，展现普通劳动者的生活与奋斗。', 'A novel by Lu Yao portraying the lives and struggles of ordinary workers.'],
        ['钱钟书长篇小说，以幽默笔触描写人生与婚姻。', 'A novel by Qian Zhongshu satirically depicting life and marriage.'],
        ['曹雪芹古典名著，描绘贾府兴衰与人物命运。', 'A classic novel by Cao Xueqin depicting the rise and fall of the Jia family.'],
        ['加西亚·马尔克斯代表作，讲述布恩迪亚家族的百年历史。', "Gabriel Garcia Marquez's masterpiece about the Buendia family over a century."],
        ['尤瓦尔·赫拉利作品，从历史视角探讨人类文明的发展。', 'Yuval Noah Harari explores the history and development of human civilization.'],
        ['以图解方式介绍常用算法与数据结构，适合编程初学者。', 'An illustrated guide to common algorithms and data structures for beginners.'],
        ['介绍程序执行、存储层次、链接和系统级编程。', 'Covers program execution, memory hierarchy, linking, and systems programming.'],
        ['介绍面向对象软件设计中的常用模式及应用场景。', 'Introduces common object-oriented design patterns and their applications.']
    ];
    const translations = new Map();
    pairs.forEach(pair => pair.forEach(text => translations.set(text, pair)));
    // Translate the template's English footer as well.
    const footer = ['版权所有 © 2021。保留所有权利。', 'Copyright © 2021.Company name All rights reserved.'];
    footer.forEach(text => translations.set(text, footer));
    let language = 'zh';
    const originalText = new WeakMap();
    const originalAttributes = new WeakMap();
    try { language = localStorage.getItem('bookmanage.language') === 'en' ? 'en' : 'zh'; } catch (_) { /* Storage may be disabled. */ }

    function translate(text) {
        const key = text.trim();
        const pair = translations.get(key);
        let value = pair ? pair[language === 'en' ? 1 : 0] : key;
        const year = key.match(/^(\d{4})级$/) || key.match(/^Class of (\d{4})$/);
        if (year) value = language === 'en' ? `Class of ${year[1]}` : `${year[1]}级`;
        return key ? text.replace(key, value) : text;
    }

    function render() {
        document.documentElement.lang = language === 'en' ? 'en' : 'zh-CN';
        const walker = document.createTreeWalker(document.documentElement, NodeFilter.SHOW_TEXT);
        let node;
        while ((node = walker.nextNode())) {
            if (node.parentElement.closest('script, style, textarea, [data-language-switch]')) continue;
            if (!originalText.has(node)) originalText.set(node, node.nodeValue);
            const text = translate(originalText.get(node));
            if (text !== node.nodeValue) node.nodeValue = text;
        }
        document.querySelectorAll('[placeholder], [aria-label], [title], img[alt]').forEach(element => {
            if (element.closest('[data-language-switch]')) return;
            if (!originalAttributes.has(element)) originalAttributes.set(element, {});
            const originals = originalAttributes.get(element);
            ['placeholder', 'aria-label', 'title', 'alt'].forEach(attribute => {
                if (element.hasAttribute(attribute)) {
                    if (!(attribute in originals)) originals[attribute] = element.getAttribute(attribute);
                    element.setAttribute(attribute, translate(originals[attribute]));
                }
            });
        });
        document.querySelectorAll('[data-language-switch] button').forEach(button => {
            button.setAttribute('aria-pressed', String(button.dataset.language === language));
            button.style.fontWeight = button.dataset.language === language ? '700' : '400';
            button.style.background = button.dataset.language === language ? '#e6efff' : '#fff';
        });
    }

    function init() {
        const switcher = document.createElement('nav');
        switcher.dataset.languageSwitch = '';
        switcher.setAttribute('aria-label', '语言 / Language');
        switcher.style.cssText = 'position:fixed;right:20px;bottom:20px;z-index:10000;display:flex;gap:4px;padding:6px;background:#fff;border:1px solid #ccd5e0;border-radius:8px;box-shadow:0 2px 8px #0002';
        [['zh', '中文'], ['en', 'English']].forEach(([value, label]) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.dataset.language = value;
            button.textContent = label;
            button.lang = value === 'zh' ? 'zh-CN' : 'en';
            button.style.cssText = 'border:0;border-radius:4px;padding:8px 12px;color:#16345c;cursor:pointer';
            button.addEventListener('click', () => {
                language = value;
                try { localStorage.setItem('bookmanage.language', value); } catch (_) { /* Keep selection for this page. */ }
                render();
            });
            switcher.appendChild(button);
        });
        document.body.appendChild(switcher);
        render();
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();
})();
