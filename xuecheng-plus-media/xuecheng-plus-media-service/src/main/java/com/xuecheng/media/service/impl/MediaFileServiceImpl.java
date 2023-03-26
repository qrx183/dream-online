package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.exception.XcPlusException;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.model.*;
import com.xuecheng.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.model.PageResult;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.print.attribute.standard.Media;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String mediaFiles;

    @Value("${minio.bucket.videofiles}")
    private String videoFiles;
    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadMediaFiles(long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        // 将文件上传到minio
        String fileName = uploadFileParamsDto.getFilename();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        String mimeType = getMimeType(extension);

        String defaultFolderPath = getDefaultFolderPath();
        
        String fileMd5 = getFileMd5(new File(localFilePath));


        // 2023/03/26/xx.jpg
        String objectName = defaultFolderPath + fileMd5 + extension;

        boolean result = addMediaFilesToMinio(localFilePath,mimeType,mediaFiles,objectName);
        if (!result) {
            XcPlusException.cast("上传文件失败");
        }

        // 这里需要将类本身注入到类中,保证调用有事务的类对象是一个代理对象
        // 开启事务的方法是来自于从IOC容器中注入的MediaFilesMapper
        // 想要保证事务有效,需要保证调用该食物方法的MediaFileService也是IOC容器注入的对象
        MediaFiles media = currentProxy.addMediaFilesToDb(companyId,fileMd5,uploadFileParamsDto,mediaFiles,objectName);

        if (media == null) {
            XcPlusException.cast("文件上传后保存信息失败");
        }
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();

        BeanUtils.copyProperties(media,uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Transactional
    @Override
    public MediaFiles addMediaFilesToDb(long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles == null){
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);

            mediaFiles.setId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setUrl("/" +bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");
            int i = mediaFilesMapper.insert(mediaFiles);
            if (i <= 0) {
                log.debug("向数据库保存文件失败,bucket:{},objectName:{}",bucket,objectName);
            }
            return mediaFiles;
        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);

        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();

            String filePath = mediaFiles.getFilePath();

            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();

            try {
                GetObjectResponse fileInputStream = minioClient.getObject(getObjectArgs);
                if (fileInputStream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return RestResponse.success(false);

    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunk) {
        // 分块文件存储路径:Md5[0]/Md5[1]/Md5.extension
        String filePath = getChunkFilePath(fileMd5,chunk);
        String bucket = videoFiles;


        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(filePath)
                .build();

        try {
            GetObjectResponse fileInputStream = minioClient.getObject(getObjectArgs);
            if (fileInputStream != null) {
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, String localFilePath, int chunk) {
        String mimeType = getMimeType(null);

        String chunkFilePath = getChunkFilePath(fileMd5,chunk);
        boolean b = addMediaFilesToMinio(localFilePath, mimeType, videoFiles, chunkFilePath);

        if (!b) {
            return RestResponse.validfail(false,"上传分块文件失败");
        }
        return RestResponse.success(true,"上传分块文件成功");
    }

    private String getChunkFilePath(String fileMd5,int chunk) {
        return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/" +chunk;
    }


    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 蒋当前日期转化成文件的形式  2023-3-26 ===>  2023/3/26/
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-","/") + "/";
        return folder;
    }

    /**
     * 上传文件到minio
     * @param localFilePath
     * @param mimeType
     * @param bucket
     * @param objectName
     * @return
     */
    private boolean addMediaFilesToMinio(String localFilePath,String mimeType, String bucket,String objectName){
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}",bucket,objectName,e.getMessage());
        }

        return false;

    }

    /**
     * 根据扩展名获取mimeType
     * @param extension 拓展名
     * @return
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }

        ContentInfo extentMatch = ContentInfoUtil.findExtensionMatch(extension);

        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        if (extentMatch != null) {
            mimeType = extentMatch.getMimeType();
        }
        return mimeType;
    }
}
