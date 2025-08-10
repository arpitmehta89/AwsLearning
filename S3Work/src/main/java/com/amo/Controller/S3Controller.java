package com.amo.Controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amo.model.ApiResponse;
import com.amo.service.S3Service;

@RestController
@RequestMapping("/api/files")

public class S3Controller {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(S3Controller.class);
	
    @Autowired
    private S3Service s3Service;
    
    

    
    
    
    @PostMapping("/uploadDocs")
    public ResponseEntity<ApiResponse> uploadDocs(@RequestParam("file") MultipartFile file) throws IOException{
    	
    	try {
			
    		ApiResponse apiResponse = s3Service.uploadDocs(file.getOriginalFilename()	, file.getBytes());
    		
    		if(apiResponse.isSuccess()) {
    			return ResponseEntity.ok(apiResponse);
    			
    		}
    		else {
    			return ResponseEntity.status(500).body(apiResponse);
    		}
    		
		} catch (Exception e) {
			
				e.printStackTrace();
		}
    	
    	return null;
    }

    @GetMapping("/listDocs")
    public ResponseEntity<List<String>> listFiles() {
    	//return ResponseEntity.status(200);
    	try {
    		List<String>list = s3Service.listFiles();
            return ResponseEntity.ok(list);
            	
		} catch (Exception e) {
			return ResponseEntity.status(500).body(null);
		}
    	
    }
    
    


	@GetMapping("/downloadDocs")
	public ResponseEntity<byte[]> downloadFile(@RequestParam("key") String key) {
		byte[] FileData = s3Service.downloadDocs(key);

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
				.body(FileData);
	}
}
