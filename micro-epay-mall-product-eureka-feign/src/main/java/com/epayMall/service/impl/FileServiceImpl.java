package com.epayMall.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.epayMall.common.ServerResponse;
import com.epayMall.service.IFileService;
import com.epayMall.util.Constants;
import com.epayMall.util.FTPUtil;

@Service("iFileService")
public class FileServiceImpl implements IFileService {
	private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

	@Override
	public <T> ServerResponse<T> uploadFile(MultipartFile file, String path) {
		ServerResponse response;
		// 1.先将文件上传到servlet容器的upload路径当中
		//需要给文件重新命名，避免上传到ftp服务器时相同命名的文件覆盖的情况
		String fileName = file.getOriginalFilename();
		String suffix = fileName.substring(fileName.indexOf(".")+1);
		String prefix = UUID.randomUUID().toString();
		String renewFileName = new StringBuilder().append(prefix).append(".").append(suffix).toString();
		logger.info("[用户上传原文件名称-{}，新文件名称-{}]", fileName, renewFileName);
		
		File fileInSelvletDir = new File(path);
		if (!fileInSelvletDir.exists()) {
			fileInSelvletDir.setWritable(true);
			fileInSelvletDir.mkdirs();
		}
		
		File targrtFile = new File(fileInSelvletDir, renewFileName);
		try {
			file.transferTo(targrtFile);
			
			//2.再将文件通过ftp上传到文件服务器上
			List<File> fileList = new ArrayList<>();
			fileList.add(targrtFile);
			boolean isuploaded = FTPUtil.uploadFile(fileList);
			
			//3.删除servlet容器中上传的文件，并且回传uri和url
			Map<String, String> map = new HashMap<>();
			if (isuploaded) {
				targrtFile.delete();
				String url = new StringBuilder().append(Constants.getProperty("ftp.server.http.prefix")).append(renewFileName).toString();
				map.put("uri", renewFileName);
				map.put("url", url);
				response = ServerResponse.createBySucessResReturnData(map);
				return response;
			} else {
				response = ServerResponse.createByErrorResReturnMsg("上传文件失败");
				return response;
			}
			
		} catch (IllegalStateException | IOException e) {
			logger.error("上传文件失败",e);
			response = ServerResponse.createByErrorResReturnMsg("上传文件失败,失败原因："+e);
			return response;
		}
	}
}
