<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">    

<html>
<body>

<h3>上传图片测试</h3>
<form action="manage/product/upload.do" method="post" enctype="multipart/form-data">
	<input type="file" name="upload_file" />
	<input type="submit" value="上传图片" />
</form>

<form action="manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
	<input type="file" name="upload_file" />
	<input type="submit" value="上传富文本图片" />
</form>

</body>
</html>
