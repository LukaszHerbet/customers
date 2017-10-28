package com.herbet.ffm.control;

import com.herbet.ffm.exception.ApplicationException;
import com.herbet.ffm.service.UploadCustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.logging.Logger;

@Controller
public class FileUploadController {

    Logger logger = Logger.getLogger(FileUploadController.class.getName());

    @Autowired
    private UploadCustomersService uploadCustomersService;

    @GetMapping("/")
    public String getUploadForm() {

        return "upload";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException, ApplicationException {

        uploadCustomersService.uploadCustomersFromFile(file);

        return "redirect:/customers";
    }
}
