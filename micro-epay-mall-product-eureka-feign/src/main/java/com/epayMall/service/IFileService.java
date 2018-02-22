package com.epayMall.service;

import org.springframework.web.multipart.MultipartFile;

import com.epayMall.common.ServerResponse;

public interface IFileService {

	<T> ServerResponse<T> uploadFile(MultipartFile file, String path);

}
