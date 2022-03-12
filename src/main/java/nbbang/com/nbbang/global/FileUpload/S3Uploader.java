package nbbang.com.nbbang.global.FileUpload;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName, String storeName) throws IOException {
        File uploadFile = convert(multipartFile).orElseThrow(() -> new IllegalArgumentException("파일 변환에 실패하였습니다. 10MB 이하의 파일이고 올바른 확장자 (png, jpeg, jpg, jfif) 여야 합니다."));
        return upload(uploadFile, dirName, storeName);
    }

    private String upload(File uploadFile, String dirName, String storeName) {
        String fileName = dirName + "/" + storeName;
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    public void delete(String dirName, String fileName) {
        log.info("aws 에 삭제 요청을 보냈습니다.");
        amazonS3Client.deleteObject(bucket, dirName + "/" + fileName);

    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("aws 에 업로드 요청을 보냈습니다.");
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("로컬 파일이 삭제되었습니다.");
        } else {
            log.info("로컬 파일이 삭제되지 못했습니다.");
        }
    }
    
    // 확장자 대문자도 됨 -> 대소문자 구분 없애기
    private Optional<File> convert(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (!(extension.equals("jpeg") || extension.equals("png") || extension.equals("jpg") || extension.equals("jfif"))) {
            return Optional.empty();
        }
        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }

        return Optional.empty();
    }
}
