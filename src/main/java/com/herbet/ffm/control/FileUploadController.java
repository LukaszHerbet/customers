package com.herbet.ffm.control;

import org.apache.commons.lang3.text.StrBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

@Controller
public class FileUploadController {

    Logger logger = Logger.getLogger(FileUploadController.class.getName());

    @GetMapping("/")
    public String getUploadForm() {

        return "upload";
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
            throws IOException {

        StrBuilder messageBuilder = new StrBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            br.lines().forEach(line -> messageBuilder.append(line).appendNewLine());
        }
        redirectAttributes.addFlashAttribute("message", messageBuilder.toString());
        return "redirect:/";
    }

}
