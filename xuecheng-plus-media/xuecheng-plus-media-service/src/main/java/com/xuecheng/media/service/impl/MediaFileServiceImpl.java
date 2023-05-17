package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */

@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

 @Autowired
 MediaProcessMapper mediaProcessMapper;
 @Autowired
 MediaFilesMapper mediaFilesMapper;

 @Autowired
 MinioClient minioClient;

 //事务
 @Autowired
 MediaFileService currentProxy;

 //存储普通文件
 @Value("${minio.bucket.files}")
 private String bucket_mediafiles;

 //存储视频
 @Value("${minio.bucket.videofiles}")
 private String bucket_video;

 @Override
 public MediaFiles getById(String mediaId) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
  return mediaFiles;
 }

 /**
  * 媒资信息分页查询
  * @param companyId
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return
  */
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

 /**
  *
  * @param companyId 机构id
  * @param uploadFileParamsDto 上传文件信息
  * @param localFilePath 文件磁盘路径
  * @param objectName  如果传入了objectName就按照objectName的目录去存储，否则按照年月日存储
  * @return
  */
 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
  File file = new File(localFilePath);
  if (!file.exists()) {
   throw new XueChengPlusException("文件不存在！");
  }
  //文件名称
  String filename = uploadFileParamsDto.getFilename();
  //文件扩展名
  String exension = filename.substring(filename.lastIndexOf("."));
  //文件的mimeType
  String mimeType =getMimeType(exension);
  //文件的MD5值
  String fileMd5 = getFileMd5(file);
  //文件的默认目录
  String defaultFolderPath = getDefaultFolderPath();
  //存储到minio中的对象名(带目录)
  if(StringUtils.isEmpty(objectName)){
    objectName = defaultFolderPath + fileMd5 + exension;
  }

  String bucket_files=bucket_mediafiles;
  //将文件上传到minio
  boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_files, objectName);
  //文件大小
  uploadFileParamsDto.setFileSize(file.length());
  //将文件信息存储到数据库
  MediaFiles mediaFiles =currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);
  //准备返回数据
  UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
  BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
  return uploadFileResultDto;
 }

 /**
  * 检查文件是否存在
  * @param fileMd5 文件的md5
  * @return
  */
 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  //先查询数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if(mediaFiles!=null){
   String bucket = mediaFiles.getBucket();
   String filePath = mediaFiles.getFilePath();
   InputStream stream = null;
   try {
    stream=minioClient.getObject(GetObjectArgs.builder()
            .bucket(bucket)
            .object(filePath)
            .build());
   } catch (Exception e) {

  }
   if(stream!=null){
    //文件已存在
    return RestResponse.success(true);
   }
  }
  return RestResponse.success(false);
 }

 /**
  * 检查分块是否存在
  * @param fileMd5  文件的md5
  * @param chunkIndex  分块序号
  * @return
  */
 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
  String chunkFileFolderPath =getChunkFileFolderPath(fileMd5);
  String chunkFilePath=chunkFileFolderPath+chunkIndex;
  InputStream stream=null;
  try {
   stream=minioClient.getObject(GetObjectArgs.builder()
           .bucket(bucket_video)
           .object(chunkFilePath)
           .build());
   if(stream!=null){
    return RestResponse.success(true);
   }
  } catch (Exception e) {
  }
  return RestResponse.success(false);
 }

 /**
  * 上传分块
  * @param fileMd5  文件md5
  * @param chunk  分块序号
  * @param localFilePath  分块文件本地地址
  * @return
  */
 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath) {
  //得到上传分块文件的FolderPath
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //分块文件的绝对地址
  String chunkFilePath=chunkFileFolderPath+chunk;

  String mimeType=getMimeType(null);
  boolean b = addMediaFilesToMinIO(localFilePath, mimeType, bucket_video, chunkFilePath);
  if(!b){
   return RestResponse.validfail(false,"上传分块文件失败");
  }
  //上传成功
  return RestResponse.success(true);

 }

 /**
  * 合并分块
  * @param companyId  机构id
  * @param fileMd5  文件md5
  * @param chunkTotal 分块总和
  * @param uploadFileParamsDto 文件信息
  * @return
  */
 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
  //校验MinIO里边是否已经有了要合并的文件(上传相同文件)
  String filename = uploadFileParamsDto.getFilename();
  String exension=filename.substring(filename.lastIndexOf("."));
  String chunkFileFolderPath=getChunkFileFolderPath(fileMd5);
  //得到文件在MinIO里边的路径
  String objectPath=getFilePathByMd5(fileMd5,exension);
  //查询参数
  GetObjectArgs getObjectArgs = GetObjectArgs.builder()
          .bucket(bucket_video).object(objectPath).build();
  try {
   //查询是否存在
   GetObjectResponse object = minioClient.getObject(getObjectArgs);
   if(object!=null){
    //MinIO已经存在，删除第二次上传的分块
    clearChunkFiles(chunkFileFolderPath,chunkTotal);
    //直接返回，不用再合并，否则会出错
    return RestResponse.success(true);
   }
  } catch (Exception e) {
   e.printStackTrace();
  }
  //MinIO没有要合并的文件
   List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_video).object(chunkFileFolderPath + i).build()).collect(Collectors.toList());
   String objectName=getFilePathByMd5(fileMd5,exension);


  try {
   ObjectWriteResponse response = minioClient.composeObject(
           ComposeObjectArgs.builder()
                   .bucket(bucket_video)
                   .object(objectName)
                   .sources(sources)
                   .build());
   log.debug("合并文件成功:{}",objectName);

  } catch (Exception e) {
       e.printStackTrace();
       log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}",bucket_video,objectName,e.getMessage());
       return RestResponse.validfail(false,"合并文件异常");
  }
  //===========校验合并后的和源文件是否一致，视频上传才成功===========
  //先下载合并后的文件
  File fileDownLoad=downloadFileFromMinIO(bucket_video,objectName);
  try(FileInputStream fileInputStream = new FileInputStream(fileDownLoad)) {
    //计算合并后文件的Md5
   String mergeFile_Md5 = DigestUtils.md5Hex(fileInputStream);
   if(!fileMd5.equals(mergeFile_Md5)){
    log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}",fileMd5,mergeFile_Md5);
    return RestResponse.validfail(false,"文件校验失败");
   }
   uploadFileParamsDto.setFileSize(fileDownLoad.length());

  }catch (Exception e){
   return RestResponse.validfail(false,"文件校验失败");
  }

  //================文件信息入库==================
  MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
  if(mediaFiles==null){
   return RestResponse.validfail(false,"文件入口失败");
  }

  //=================清理分块文件
  clearChunkFiles(chunkFileFolderPath,chunkTotal);
  return RestResponse.success(true);


 }

 /**
  * 清理分块文件
  * @param chunkFileFolderPath
  * @param chunkTotal
  */
 private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
  Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> new DeleteObject(chunkFileFolderPath + i)).collect(Collectors.toList());
  RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objects).build();
  Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
  //真正删除
  results.forEach(f->{
   try {
    DeleteError deleteError=f.get();
   } catch (Exception e) {
      e.printStackTrace();
   }
  });
 }

 /**
  *先下载合并后的文件
  * @param bucketVideo
  * @param objectName
  * @return
  */
 public File downloadFileFromMinIO(String bucketVideo, String objectName) {
  //临时文件
  File tempFile = null;
  FileOutputStream outputStream = null;
  try {
   InputStream inputStream=minioClient.getObject(GetObjectArgs.builder().
           bucket(bucketVideo)
           .object(objectName)
           .build());
   tempFile = File.createTempFile("minio", "merge");
   outputStream = new FileOutputStream(tempFile);
   IOUtils.copy(inputStream,outputStream);
   return tempFile;
  } catch (Exception e) {
   e.printStackTrace();
  }finally {
    if(outputStream!=null){
     try {
      outputStream.close();
     } catch (IOException e) {
      throw new RuntimeException(e);
     }
    }
  }
   return null;

 }

 /**
  * 分块合并后的文件的名称
  * @param fileMd5
  * @param exension
  * @return
  */
 private String getFilePathByMd5(String fileMd5, String exension) {
    return fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/" +fileMd5 +exension;
 }

 /**
  * 根据分块文件的Md5值得到分块文件地址
  * @param fileMd5
  * @return
  */
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+"chunk"+"/";
 }

 /**
  * @description 将文件信息添加到文件表
  * @param companyId  机构id
  * @param fileMd5  文件md5值
  * @param uploadFileParamsDto  上传文件的信息
  * @param bucket  桶
  * @param objectName 对象名称
  * @return com.xuecheng.media.model.po.MediaFiles
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
  //从数据库查询文件
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileMd5);
   mediaFiles.setFileId(fileMd5);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert < 0) {
    log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
    throw new XueChengPlusException("保存文件信息失败");
   }
   //添加到待处理任务表
   addWaitingTask(mediaFiles);
   log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());
  }
  return mediaFiles;
 }

 private void addWaitingTask(MediaFiles mediaFiles) {
  //文件名称
  String filename = mediaFiles.getFilename();
  //文件扩展名
  String extension=filename.substring(filename.lastIndexOf("."));
  //mimeType
  String mimeType = getMimeType(extension);
  if(mimeType.equals("video/x-msvideo")){
   MediaProcess mediaProcess = new MediaProcess();
   BeanUtils.copyProperties(mediaFiles,mediaProcess);
   mediaProcess.setStatus("1");//未处理
   mediaProcess.setFailCount(0);//失败次数默认为0
   mediaProcessMapper.insert(mediaProcess);
  }
 }


 /**
  * @description 将文件写入minIO
  * @param localFilePath  文件地址
  * @param bucket  桶
  * @param objectName 对象名称
  * @return void
  * @author Mr.M
  * @date 2022/10/12 21:22
  */
 public boolean addMediaFilesToMinIO(String localFilePath,String mimeType,String bucket, String objectName) {
  try {
   UploadObjectArgs testbucket = UploadObjectArgs.builder()
           .bucket(bucket)
           .object(objectName)
           .filename(localFilePath)
           .contentType(mimeType)
           .build();
   minioClient.uploadObject(testbucket);
   log.debug("上传文件到minio成功,bucket:{},objectName:{}",bucket,objectName);
   System.out.println("上传成功");
   return true;
  } catch (Exception e) {
   e.printStackTrace();
   log.error("上传文件到minio出错,bucket:{},objectName:{},错误原因:{}",bucket,objectName,e.getMessage(),e);
   throw new XueChengPlusException("上传文件到文件系统失败");
  }
 }


 //获取文件默认存储目录路径 年/月/日
 private String getDefaultFolderPath() {
  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
  String folder = sdf.format(new Date()).replace("-", "/")+"/";
  return folder;
 }

 /**
  * 得到文件的Md5值
  *
  * @param file
  * @return
  */
 private String getFileMd5(File file) {
  try (FileInputStream fileInputStream = new FileInputStream(file)) {
   String fileMd5 = DigestUtils.md5Hex(fileInputStream);
   return fileMd5;
  } catch (Exception e) {
   e.printStackTrace();
   return null;
  }
 }

 /**
  * 根据扩展名获得mimetype
  * @param entension
  * @return
  */
 private String getMimeType(String entension) {
  if(entension==null){
   entension="";
  }
  //根据扩展名取出mimeType
  ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(entension);
  String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
  if(extensionMatch!=null){
   mimeType=extensionMatch.getMimeType();
  }
  return mimeType;
 }

}