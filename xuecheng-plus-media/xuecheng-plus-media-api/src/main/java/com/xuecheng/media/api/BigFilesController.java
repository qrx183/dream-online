package com.xuecheng.media.api;


import com.xuecheng.exception.RestErrorResponse;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.model.RestResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;
import java.io.File;

@Api(value = "大文件上传接口",tags = "大文件上传接口")
@RestController
public class BigFilesController {

    @Autowired
    private MediaFileService mediaFileService;

    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkFile(@RequestParam("fileMd5") String fileMd5) {
        return mediaFileService.checkFile(fileMd5);
    }

    @ApiOperation(value = "分块文件上传前检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkChunk(@RequestParam("fileMd5") String fileMd5,@RequestParam("chunk") int chunk) {
        return mediaFileService.checkChunk(fileMd5,chunk);
    }

    @SneakyThrows
    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadChunk(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("file")MultipartFile file,
                                    @RequestParam("chunk") int chunk) {
        // 后端只处理每个分块的上传逻辑,由前端判断一个文件的分块是否存在,没有存在,调用该分块的上传接口
        File tempFile = File.createTempFile("minio",".temp");
        file.transferTo(tempFile);

        String localFilePath = tempFile.getAbsolutePath();

        return mediaFileService.uploadChunk(fileMd5,localFilePath,chunk);

    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestErrorResponse mergeChunks(@RequestParam("fileMd5") String fileMd5,
                                         @RequestParam("fileName") String fileName,
                                         @RequestParam("chunkTotal") int chunkTotal) {
        return null;
    }


}
