package com.example.servicetest.service;

import com.example.servicetest.entity.AffectationTest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTestResultEmail(String toEmail,
                                    String freelancerName,
                                    AffectationTest affectation) {
        // Si pas d'email fourni, on utilise une adresse de test par défaut
        if (toEmail == null || toEmail.isBlank()) {
            toEmail = "garrammbrahim@gmail.com";
        }
        if (affectation == null) {
            return;
        }

        String name = (freelancerName == null || freelancerName.isBlank())
                ? "Freelancer"
                : freelancerName;

        int score = affectation.getScore() != null ? affectation.getScore().intValue() : 0;
        boolean accepted = Boolean.TRUE.equals(affectation.getIsValidated());
        String decisionLabel = accepted ? "ACCEPTED" : "NOT ACCEPTED";
        String decisionColor = accepted ? "#16a34a" : "#dc2626";
        String decisionBg = accepted ? "rgba(22,163,74,0.10)" : "rgba(220,38,38,0.08)";
        String decisionBorder = accepted ? "rgba(22,163,74,0.35)" : "rgba(220,38,38,0.30)";

        int totalQuestions = affectation.getTotalQuestions() != null ? affectation.getTotalQuestions() : 0;
        int correct = affectation.getCorrectAnswersCount() != null ? affectation.getCorrectAnswersCount() : 0;
        int timeSpentSec = affectation.getTimeSpent() != null ? affectation.getTimeSpent() : 0;
        int minutes = timeSpentSec / 60;
        int seconds = timeSpentSec % 60;
        String timeLabel = minutes > 0
                ? minutes + " min " + seconds + " s"
                : seconds + " s";

        String dateLabel = affectation.getTestDate() != null
                ? affectation.getTestDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "-";

        String subject = "Your qualification test result";

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Your test result</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f5f4f0;font-family:'Helvetica Neue',Helvetica,Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="background-color:#f5f4f0;min-height:100vh;">
                    <tr>
                      <td align="center" style="padding:40px 16px;">

                        <!-- CARD -->
                        <table width="620" cellpadding="0" cellspacing="0" border="0" style="max-width:620px;width:100%%;border-radius:20px;overflow:hidden;border:1px solid #e8e4dc;box-shadow:0 8px 40px rgba(0,0,0,0.10);">

                          <!-- Orange top bar -->
                          <tr><td style="height:4px;background:linear-gradient(90deg,#ff6b35 0%%,#ff9a5c 50%%,#ff6b35 100%%);"></td></tr>

                          <!-- HEADER -->
                          <tr>
                            <td style="background-color:#ffffff;padding:24px 32px;">
                              <table cellpadding="0" cellspacing="0" border="0" width="100%%">
                                <tr>
                                  <td>
                                    <table cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td style="vertical-align:middle;">
                                          <div style="width:44px;height:44px;background:linear-gradient(135deg,#ff6b35 0%%,#ff4500 100%%);border-radius:12px;text-align:center;line-height:44px;font-size:22px;font-weight:900;color:#ffffff;display:inline-block;">F</div>
                                        </td>
                                        <td style="vertical-align:middle;padding-left:14px;">
                                          <div style="font-size:17px;font-weight:800;color:#111111;letter-spacing:0.06em;text-transform:uppercase;line-height:1;">FREELANCIFY</div>
                                          <div style="font-size:11px;color:#aaa8a0;letter-spacing:0.1em;text-transform:uppercase;margin-top:3px;">Qualification platform</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td align="right" style="vertical-align:middle;">
                                    <div style="display:inline-block;padding:6px 16px;border-radius:999px;background:%s;border:1px solid %s;font-size:11px;font-weight:700;color:%s;letter-spacing:0.14em;text-transform:uppercase;">%s</div>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <!-- DIVIDER -->
                          <tr><td style="height:1px;background:#f0ede6;"></td></tr>

                          <!-- BODY -->
                          <tr>
                            <td style="background-color:#fafaf8;padding:32px 32px 28px;">

                              <p style="margin:0 0 6px;font-size:22px;font-weight:700;color:#111111;">Hello %s,</p>
                              <p style="margin:0 0 28px;font-size:14px;color:#888480;line-height:1.75;">
                                Thank you for completing your qualification test on our platform.<br>
                                Here is a summary of your results.
                              </p>

                              <!-- SCORE BLOCK -->
                              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom:20px;">
                                <tr>
                                  <td style="background:linear-gradient(135deg,#ff6b35 0%%,#1d4ed8 100%%);border-radius:16px;padding:1px;">
                                    <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td style="background:#ffffff;border-radius:15px;padding:22px 26px;">
                                          <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                              <td style="vertical-align:middle;">
                                                <div style="font-size:11px;color:#bbb8b0;text-transform:uppercase;letter-spacing:0.16em;font-weight:600;margin-bottom:8px;">Score</div>
                                                <div style="font-size:54px;font-weight:900;color:#111111;line-height:1;letter-spacing:-2px;">%d<span style="font-size:24px;color:#ff6b35;margin-left:2px;">%%</span></div>
                                                <div style="margin-top:8px;font-size:13px;color:#aaa8a0;">
                                                  <span style="color:#111111;font-weight:700;">%d</span>
                                                  <span> correct answer(s) out of </span>
                                                  <span style="color:#111111;font-weight:700;">%d</span>
                                                  <span> question(s)</span>
                                                </div>
                                              </td>
                                              <td align="right" style="vertical-align:middle;">
                                                <div style="display:inline-block;padding:10px 22px;border-radius:999px;background:%s;border:1px solid %s;font-size:12px;font-weight:800;color:%s;letter-spacing:0.12em;text-transform:uppercase;white-space:nowrap;">%s</div>
                                              </td>
                                            </tr>
                                          </table>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                              <!-- METRICS -->
                              <table width="100%%" cellpadding="0" cellspacing="0" border="0" style="margin-bottom:24px;">
                                <tr>
                                  <td width="48%%" style="vertical-align:top;">
                                    <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td style="background:#ffffff;border:1px solid #e8e4dc;border-radius:12px;padding:14px 18px;">
                                          <div style="font-size:10px;color:#bbb8b0;text-transform:uppercase;letter-spacing:0.16em;font-weight:600;margin-bottom:6px;">&#128197; Test date</div>
                                          <div style="font-size:14px;font-weight:700;color:#1a1a1a;">%s</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td width="4%%"></td>
                                  <td width="48%%" style="vertical-align:top;">
                                    <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                      <tr>
                                        <td style="background:#ffffff;border:1px solid #e8e4dc;border-radius:12px;padding:14px 18px;">
                                          <div style="font-size:10px;color:#bbb8b0;text-transform:uppercase;letter-spacing:0.16em;font-weight:600;margin-bottom:6px;">&#9201; Time spent</div>
                                          <div style="font-size:14px;font-weight:700;color:#1a1a1a;">%s</div>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                              <!-- CTA -->
                              <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                <tr>
                                  <td style="background:%s;border:1px solid %s;border-radius:12px;padding:16px 20px;">
                                    <table cellpadding="0" cellspacing="0" border="0" width="100%%">
                                      <tr>
                                        <td width="3" style="background:%s;border-radius:3px;">&nbsp;</td>
                                        <td style="padding-left:14px;font-size:13px;color:%s;line-height:1.75;">
                                          %s
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                </tr>
                              </table>

                            </td>
                          </tr>

                          <!-- FOOTER -->
                          <tr>
                            <td style="background:#ffffff;border-top:1px solid #f0ede6;padding:20px 32px;text-align:center;">
                              <p style="margin:0 0 5px;font-size:11px;color:#bbb8b0;line-height:1.7;">
                                This email was automatically generated by the <span style="color:#ff6b35;font-weight:700;">Freelancify</span> platform after a technical test.<br>
                                If you believe you received this message by error, please ignore it.
                              </p>
                              <p style="margin:0;font-size:11px;color:#ccc9c0;">For any questions, contact our support team.</p>
                            </td>
                          </tr>

                          <!-- Orange bottom bar -->
                          <tr><td style="height:4px;background:linear-gradient(90deg,#ff6b35 0%%,#ff9a5c 50%%,#ff6b35 100%%);"></td></tr>

                        </table>

                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                // Header badge
                decisionBg, decisionBorder, decisionColor, decisionLabel,
                // Greeting
                name,
                // Score
                score, correct, totalQuestions,
                // Decision pill in score block
                decisionBg, decisionBorder, decisionColor, decisionLabel,
                // Metrics
                dateLabel,
                timeLabel,
                // CTA: bg, border, accent bar color, text color, message
                accepted ? "rgba(22,163,74,0.07)" : "rgba(29,78,216,0.06)",
                accepted ? "rgba(22,163,74,0.22)" : "rgba(29,78,216,0.20)",
                accepted ? "#16a34a" : "#1d4ed8",
                accepted ? "#14532d" : "#1e3a8a",
                accepted
                        ? "Congratulations! Your profile is now <strong style='color:#16a34a;'>eligible</strong> for the next steps. Our team may contact you soon to continue the process."
                        : "Your score is below the pass threshold. You may retry later if the platform rules allow it."
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Erreur lors de l'envoi de l'e-mail de résultat : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
