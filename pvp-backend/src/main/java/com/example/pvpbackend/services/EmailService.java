package com.example.pvpbackend.services;

import com.example.pvpbackend.models.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    private final AddressService addressService;
    private final UsersService usersService;
    public EmailService(UsersService usersService, AddressService addressService){
        this.usersService = usersService;
        this.addressService = addressService;
    }
    public void sendEmailWithZip(byte[] zipData, String fileName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        User user = usersService.getCurrent();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getElPastas());
        helper.setSubject("Jūsų RKPC.LT šiukšlių vežimo grafikai");
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"lt\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<p>Sveiki,</p>");
        html.append("<p>prisegame ZIP archyvą su jūsų pasirinkto adreso šiukšlių išvežimo grafikais.</p>");
        html.append("<p>Jeigu turite klausimų ar pastebėjimų, susisiekite su mumis žemiau nurodytais kontaktais.</p>");
        html.append("<p style=\"margin-top: 20px;\">Pagarbiai,<br>");
        html.append("UAB Komunalinių paslaugų centras</p>");
        html.append("<footer style=\"margin-top: 30px; font-size: 0.9em; color: #555;\">");
        html.append("<p>Liepų g. 34, Garliava, LT-53206 Kauno r.<br>");
        html.append("Įm. kodas 301846604<br>");
        html.append("PVM mokėtojo kodas LT100004286913<br>");
        html.append("el. paštas: <a href=\"mailto:info@rkpc.lt\">info@rkpc.lt</a><br>");
        html.append("Tel.: 0-602 22772</p>");
        html.append("</footer>");
        html.append("</body>");
        html.append("</html>");
        helper.setText(html.toString(), true);
        InputStreamSource attachment = new ByteArrayResource(zipData);
        helper.addAttachment(fileName, attachment);

        mailSender.send(message);
    }

    public void sendNewRequestReminderEmail(Employee employee, RegisteredRequest request) throws MessagingException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getUser().getElPastas());
        helper.setSubject("Priskirtas naujas prašymas – RKPC.LT");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"lt\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 20px; }");
        html.append(".container { background-color: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("h2 { color: #007B5E; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("footer { font-size: 0.9em; color: #777; margin-top: 40px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"container\">");

        html.append("<h2>Jums priskirtas naujas prašymas</h2>");
        html.append("<p>Informuojame, kad sistema jums priskyrė naują gyventojo prašymą:</p>");

        html.append("<table>");
        html.append("<tr><th>Pavadinimas</th><td>").append(request.getPavadinimas()).append("</td></tr>");
        html.append("<tr><th>Aprašymas</th><td>").append(request.getAprasymas()).append("</td></tr>");
        html.append("<tr><th>Priskyrimo data</th><td>")
                .append(request.getData().format(formatter))
                .append("</td></tr>");
        html.append("<tr><th>Gyventojas</th><td>").append(request.getClient().getUser().getVardas()).append(" ")
                .append(request.getClient().getUser().getPavarde()).append("</td></tr>");
        html.append("</table>");

        html.append("<p>Prašome peržiūrėti užduotį sistemoje ir imtis veiksmų.</p>");

        html.append("<p>Jei turite klausimų, kreipkitės į sistemos administratorių.</p>");

        html.append("<footer>");
        html.append("<p><strong>UAB Komunalinių paslaugų centras</strong><br>");
        html.append("Liepų g. 34, Garliava, LT-53206 Kauno r.<br>");
        html.append("Įm. kodas 301846604<br>");
        html.append("PVM kodas LT100004286913<br>");
        html.append("El. paštas: <a href=\"mailto:info@rkpc.lt\">info@rkpc.lt</a><br>");
        html.append("Tel.: 0-602 22772</p>");
        html.append("</footer>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        helper.setText(html.toString(), true);
        mailSender.send(message);
    }


    public void sendNewRequestReminderEmail(Employee employee, List<RegisteredRequest> requests) throws MessagingException {
        if (requests == null || requests.isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(employee.getUser().getElPastas());
        helper.setSubject("Priskirti nauji prašymai – RKPC.LT");

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"lt\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; color: #333; background-color: #f9f9f9; padding: 20px; }");
        html.append(".container { background-color: #fff; border-radius: 8px; padding: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        html.append("h2 { color: #007B5E; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
        html.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
        html.append("footer { font-size: 0.9em; color: #777; margin-top: 40px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class=\"container\">");

        html.append("<h2>Jums priskirti nauji prašymai</h2>");
        html.append("<p>Informuojame, kad sistema jums priskyrė šiuos gyventojų prašymus:</p>");

        html.append("<table>");
        html.append("<tr><th>#</th><th>Pavadinimas</th><th>Aprašymas</th><th>Priskyrimo data</th><th>Gyventojas</th></tr>");

        int index = 1;
        for (RegisteredRequest request : requests) {
            html.append("<tr>");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td>").append(request.getPavadinimas()).append("</td>");
            html.append("<td>").append(request.getAprasymas()).append("</td>");
            html.append("<td>").append(request.getData().format(formatter)).append("</td>");
            html.append("<td>").append(request.getClient().getUser().getVardas()).append(" ")
                    .append(request.getClient().getUser().getPavarde()).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");

        html.append("<p>Prašome peržiūrėti užduotis sistemoje ir imtis veiksmų.</p>");
        html.append("<p>Jei turite klausimų, kreipkitės į sistemos administratorių.</p>");

        html.append("<footer>");
        html.append("<p><strong>UAB Komunalinių paslaugų centras</strong><br>");
        html.append("Liepų g. 34, Garliava, LT-53206 Kauno r.<br>");
        html.append("Įm. kodas 301846604<br>");
        html.append("PVM kodas LT100004286913<br>");
        html.append("El. paštas: <a href=\"mailto:info@rkpc.lt\">info@rkpc.lt</a><br>");
        html.append("Tel.: 0-602 22772</p>");
        html.append("</footer>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        helper.setText(html.toString(), true);
        mailSender.send(message);
    }


    public void sendReminderEmail(Client client, List<Contract> contracts) throws MessagingException {
        if(!client.getElPastoPriminimai()) return;
        if(contracts == null || contracts.isEmpty()) return;

        MimeMessage message = mailSender.createMimeMessage();
        User user = usersService.get(client.getIdNaudotojas());
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getElPastas());
        helper.setSubject("Priminimas apie šiukšlių išvėžimą. RKPC.LT");
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html lang=\"lt\">");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("table { border-collapse: collapse; width: 100%; }");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; }");
        html.append("th { background-color: #f2f2f2; text-align: left; }");
        html.append("footer { font-size: 0.9em; color: #555; margin-top: 30px; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<p>Sveiki,</p>");
        html.append("<p>Primename, jog <strong>rytoj</strong> išvežamos atliekos šiais adresais:</p>");

        html.append("<table>");
        html.append("<tr><th>Aprašymas</th><th>Adresas</th></tr>");
        for (Contract contract : contracts) {
            html.append("<tr>")
                    .append("<td>").append(contract.getDescription()).append("</td>")
                    .append("<td>").append(contract.getFullAddress()).append("</td>")
                    .append("</tr>");
        }
        html.append("</table>");
        html.append("<p style=\"margin-top: 20px;\">Pagarbiai,<br>");
        html.append("UAB Komunalinių paslaugų centras</p>");
        html.append("<footer>");
        html.append("<p>Liepų g. 34, Garliava, LT-53206 Kauno r.<br>");
        html.append("Įm. kodas 301846604<br>");
        html.append("PVM mokėtojo kodas LT100004286913<br>");
        html.append("el. paštas: <a href=\"mailto:info@rkpc.lt\">info@rkpc.lt</a><br>");
        html.append("Tel.: 0-602 22772</p>");
        html.append("</footer>");
        html.append("</body>");
        html.append("</html>");
        helper.setText(html.toString(), true);
        mailSender.send(message);
    }

}
