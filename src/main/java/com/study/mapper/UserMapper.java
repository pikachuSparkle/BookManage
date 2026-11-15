package com.study.mapper;

import com.study.entity.Account;
import com.study.entity.Student;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface UserMapper {
    @Select("SELECT id, username, password, role FROM user WHERE username = #{username}")
    Account findUserByName(String username);

    @Select("SELECT id, name, sex, grade FROM student ORDER BY id")
    List<Student> getStudentList();

    @Select("SELECT COUNT(*) FROM student WHERE id = #{id}")
    int countStudent(int id);

    @Select("SELECT COUNT(*) FROM student")
    int countStudents();
}
