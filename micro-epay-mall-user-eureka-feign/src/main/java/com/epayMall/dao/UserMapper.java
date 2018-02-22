package com.epayMall.dao;

import org.apache.ibatis.annotations.Param;

import com.epayMall.pojo.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    int checkIfExistsUser(String userName);
    
    User selectOneIfUserPwdIsRight(@Param("userName") String userName, @Param("pwd") String pwd);

	int checkUsername(String userName);

	int checkEmail(String email);
	
	String getForgettenQuestion(String userName);
	
	int checkAnswerIsRight(@Param("username")String username, @Param("question")String question, @Param("answer")String answer);
	
	int updateUserPassword(@Param("username")String username, @Param("passwordNew")String passwordNew);
	
	int checkPasswordByUserId(@Param("userId")Integer userId, @Param("passwordOld")String passwordOld);
	
	User selectByUserName(String username);
}