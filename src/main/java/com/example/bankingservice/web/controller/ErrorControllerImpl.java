package com.example.bankingservice.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ErrorControllerImpl implements ErrorController {
    @GetMapping("/error")
    public String handleError(HttpServletRequest req, Model model) {
        String exceptionMessage = "";
        String statusCode = "";
        Integer status = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            exceptionMessage = HttpStatus.valueOf(status).getReasonPhrase();
            statusCode = status.toString();
            if (status == HttpStatus.NOT_FOUND.value()) {
                exceptionMessage = "Page not found / Страница не найдена";
            } else if (status == HttpStatus.FORBIDDEN.value()) {
                exceptionMessage = "Sorry, you do not have permission to perform this action / " +
                        "Извините, у вас нет прав на выполнение данного действия";
            }
        }
        model.addAttribute("exceptionMessage", exceptionMessage);
        model.addAttribute("statusCode", statusCode);
        return "error";
    }

    @GetMapping("/error/{id}")
    public String handleErrorId(@PathVariable String id, Model model) {
        String exceptionMessage = "";
        String statusCode = "";
        if (!id.isEmpty()) {
            statusCode = id;
            exceptionMessage = switch (id) {
                case "401" -> "Bad Request";
                case "403" -> "Sorry, you do not have permission to perform this action";
                case "404" -> "Page not found";
                default -> "Error";
            };
        }
        model.addAttribute("exceptionMessage", exceptionMessage);
        model.addAttribute("statusCode", statusCode);
        return "error";
    }
}
