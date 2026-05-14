package com.vidyalaya.publicweb;

import com.vidyalaya.domain.repository.NoticeRepository;
import com.vidyalaya.domain.repository.PublicPageRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/public")
public class PublicWebController {

    private final PublicPageRepository publicPageRepository;
    private final NoticeRepository noticeRepository;

    public PublicWebController(PublicPageRepository publicPageRepository, NoticeRepository noticeRepository) {
        this.publicPageRepository = publicPageRepository;
        this.noticeRepository = noticeRepository;
    }

    @GetMapping("/{slug}")
    public String home(@PathVariable String slug, Model model) {
        model.addAttribute("slug", slug);
        model.addAttribute(
                "page", publicPageRepository.findByPageKey("home").orElse(null));
        return "public/home";
    }

    @GetMapping("/{slug}/about")
    public String about(@PathVariable String slug, Model model) {
        model.addAttribute("slug", slug);
        model.addAttribute(
                "page", publicPageRepository.findByPageKey("about").orElse(null));
        return "public/about";
    }

    @GetMapping("/{slug}/announcements")
    public String announcements(@PathVariable String slug, Model model) {
        model.addAttribute("slug", slug);
        model.addAttribute("notices", noticeRepository.findAll());
        return "public/announcements";
    }

    @GetMapping("/{slug}/contact")
    public String contact(@PathVariable String slug, Model model) {
        model.addAttribute("slug", slug);
        model.addAttribute(
                "page", publicPageRepository.findByPageKey("contact").orElse(null));
        return "public/contact";
    }

    @GetMapping(value = "/{slug}/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemap(@PathVariable String slug) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">"
                + "<url><loc>/public/"
                + slug
                + "</loc></url>"
                + "<url><loc>/public/"
                + slug
                + "/about</loc></url>"
                + "</urlset>";
    }
}
