package com.application.adminservice.controller;

import com.application.adminservice.dto.BookingDTO;
import com.application.adminservice.service.AdminService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/admin/reports")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    @Autowired
    private AdminService adminService;

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/book")
    public String showReportPage(Model model) {
        return "report";
    }

    @GetMapping("/booking-summary/pdf")
    public void generatePdfReport(HttpServletResponse response) {
        try {
            List<BookingDTO> booking=adminService.getAllBooking();
            logger.debug("Get all booking details are: " + booking);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(booking);

            InputStream reportStream = getClass().getResourceAsStream("/reports/booking-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "BusTicket Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=booking-summary.pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            logger.info("PDF report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF report: ", e);
            throw new RuntimeException("Failed to generate PDF report");
        }
    }

    @GetMapping("/booking-summary/excel")
    public void generateExcelReport(HttpServletResponse response) {
        try {
            List<BookingDTO> booking=adminService.getAllBooking();
            logger.debug("Get all booking details are: " + booking);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(booking);

            InputStream reportStream = getClass().getResourceAsStream("/reports/booking-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "BusTicket Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=booking-summary.xls");

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);
            exporter.exportReport();
            logger.info("Excel report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating Excel report: ", e);
            throw new RuntimeException("Failed to generate Excel report");
        }
    }
}