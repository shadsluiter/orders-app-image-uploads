package com.shadsluiter.ordersapp.controllers;

import com.shadsluiter.ordersapp.models.OrderModel;
import com.shadsluiter.ordersapp.models.OrderSearch;
import com.shadsluiter.ordersapp.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException; 
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

 


/**
 * VULNERABILITY LAB NOTE:
 * This controller is used for a secure-coding lab on file uploads.
 *
 * The current implementation intentionally demonstrates common upload mistakes:
 * - Relying on user-controlled metadata (e.g., original filenames)
 * - No server-side size limits in the controller
 * - Saving attacker-controlled content into a publicly served location
 *
 * IMPORTANT: The goal of the lab is to identify these issues and apply secure fixes
 * (size limits, safe filenames, content validation, safe storage, and safe serving).
 */

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;

    @Autowired
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String getAllOrders(Model model) {
        List<OrderModel> orders = orderService.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("message", "Showing all orders");
        model.addAttribute("pageTitle", "Orders");
        return "orders";
    }

    @GetMapping("/{id}")
    public String getOrderById(@PathVariable String id, Model model) {
        OrderModel order = orderService.findById(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("orderSearch", new OrderSearch());
        return "searchForm";
    }

    @PostMapping("/search")
    public String search(@ModelAttribute @Valid OrderSearch orderSearch, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "searchForm";
        }
        List<OrderModel> orders = orderService.findByNotes(orderSearch.getSearchString());
        model.addAttribute("message", "Search results for " + orderSearch.getSearchString());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/create")
    public String showCreateOrderForm(Model model) {
        model.addAttribute("order", new OrderModel());
        model.addAttribute("pageTitle", "Create Order");
        return "create-order";
    }

    @PostMapping("/create")
    public String createOrder(
            @ModelAttribute @Valid OrderModel order,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile imageFile,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create Order");
            return "create-order";
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                /**
                 * LAB ISSUE: No controller-enforced size limit
                 * This method does not enforce a maximum upload size. Large uploads can
                 * cause resource exhaustion (disk usage, temp file growth, bandwidth, etc.).
                 *
                 * Note: Framework-level multipart limits may still exist depending on configuration,
                 * but this controller does not enforce its own safety checks.
                 */

                String pictureUrl = saveUploadedImage(imageFile);
                order.setPictureUrl(pictureUrl);
            } catch (IOException ex) {
                model.addAttribute("pageTitle", "Create Order");
                model.addAttribute("uploadError", "Image upload failed. Please try again.");
                return "create-order";
            }
        }

        orderService.save(order);
        return "redirect:/orders";
    }

    /**
     * LAB ISSUE: Extension allow-list is not content validation
     * Checking the filename extension only verifies the *name*, not the file's real type.
     * Attackers control the filename and can upload non-image content while using an
     * image-like extension.
     *
     * Secure practice (later in the tutorial): validate content using server-side
     * inspection (e.g., magic bytes) and enforce safe serving behavior.
     */

    private String saveUploadedImage(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";

        int dot = original.indexOf('.'); // still dangerous because of multiple dots.  image.jpg.php will work in this case which is dangerous.
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase();
        }

        /**
         * VULNERABILITY: Client-Side Trust (Extension Spoofing)
         * We only check the file extension. An attacker can name a malicious script
         * 'malware.jpg.php' or 'shell.php%00.jpg' (Null Byte Injection) to bypass this.
         * SECURE PRACTICE: Check the MIME type (Magic Bytes) using a library like Apache Tika.
         */
        List<String> allowedExtensions = List.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp", ".svg");
        boolean isAllowed = false;
        for (String allowedExt : allowedExtensions) {
            if (ext.equals(allowedExt)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IOException("File type not allowed");
        }

       
        /**
         * LAB ISSUE: Using the client-supplied filename is unsafe
         * The original filename is attacker-controlled and can cause:
         * - Filename collisions (overwriting existing files)
         * - Unexpected characters or path-like input
         * - Attempts to escape the intended directory (path manipulation)
         *
         * Secure practice (later): ignore the client filename for storage and generate a safe,
         * unique name (e.g., UUID), and enforce directory constraints.
         */

        String filename = file.getOriginalFilename(); 

        
        /**
         * LAB ISSUE: Publicly served upload location
         * Saving uploads into a publicly served static directory can allow attacker-controlled
         * content to be hosted and accessed directly via URL.
         *
         * The risk is typically "hosting attacker content" (e.g., content-sniffing issues,
         * stored XSS with certain formats, or unintended file exposure), not necessarily
         * "server executes the file."
         *
         * Secure practice (later): store outside public static paths and serve files through a
         * controlled endpoint with safe headers and authorization.
         */

        Path uploadDir = Paths.get("src/main/resources/static/uploads");
        Files.createDirectories(uploadDir);

        Path destination = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + filename;
    }

    @GetMapping("/customer/{customerid}")
    public String getOrdersByCustomerId(@PathVariable String customerid, Model model) {
        List<OrderModel> orders = orderService.findByCustomerid(customerid);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/edit/{id}")
    public String showEditOrderForm(@PathVariable String id, Model model) {
        OrderModel order = orderService.findById(id);
        model.addAttribute("order", order);
        return "edit-order";
    }

    @PostMapping("/edit/{id}")
    public String updateOrder(
            @PathVariable String id,
            @ModelAttribute @Valid OrderModel order,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Order");
            return "edit-order";
        }

        order.setId(id);
        OrderModel existing = orderService.findById(id);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String pictureUrl = saveUploadedImage(imageFile);
                order.setPictureUrl(pictureUrl);
            } catch (IOException ex) {
                model.addAttribute("pageTitle", "Edit Order");
                model.addAttribute("uploadError", "Image upload failed. Please try again.");
                model.addAttribute("order", order);
                return "edit-order";
            }
        } else {
            order.setPictureUrl(existing.getPictureUrl());
        }

        orderService.save(order);
        return "redirect:/orders";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrder(@PathVariable String id) {
        orderService.delete(id);
        return "redirect:/orders";
    }
 


}