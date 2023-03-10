package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.api.model.Move;
import com.example.api.repository.MoveRepository;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMultipart;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.Data;

@Data
@Service
public class MoveService {

  @Autowired
  private MoveRepository moveRepository;

  public Move saveMove(Move move) {
    List<Move> refExist = moveRepository.findByRef(move.getRef());
    Move result = new Move();
    if (refExist.size() > 0 && !move.getInOut()) {
      result.setMsg("Une entrée avec cette référence existe déjà");
    } else if (refExist.size() == 0 && move.getInOut()) {
      result.setMsg("Il n'éxsite aucune entrée avec cette référence");
    } else if (refExist.size() > 1 && move.getInOut()) {
      result.setMsg("Une sortie avec cette référence existe déjà");
    } else { // (refExist.size() == 0 && !move.getInOut())
             // ||(refExist.size() > 0 && move.getInOut())
      result = moveRepository.save(move);
      move.setMoveDate(Instant.now().truncatedTo(ChronoUnit.MILLIS));
      this.createXMLFile(move);
    }
    return result;
  }

  public List<Move> getMoves() {
    List<Move> moves = moveRepository.findFirst50ByOrderByCreationDateDesc();
    return moves;
  }

  private void createXMLFile(Move move) {
    MustacheFactory mf = new DefaultMustacheFactory();
    StringWriter writer = new StringWriter();
    if (move.getInOut()) {
      mf.compile("CargoMessageOut.mustache").execute(writer, move);
    } else {
      mf.compile("CargoMessageIn.mustache").execute(writer, move);
    }
    /*
     * // Unique template
     * // mf.compile("CargoMessage.mustache").execute(writer, move);
     */
    try {
      FileWriter fw = new FileWriter("CargoMessage.xml");
      /*
       * // Unique template
       * fw.write(writer.toString().replaceAll("(?m)^[ \t]*\r?\n", ""));
       * // (?m) pour string avec plusieur lignes
       */
      fw.close();
    } catch (IOException ioex) {
      ioex.printStackTrace();
      System.out.println("Exception : XML file not created : " + ioex.toString());
    }
    System.out.println("Done creating XML File");
    // this.sendMail();
  }

  private void sendMail() {
    final String username = "*************";
    final String password = "***********";

    String host = "sandbox.smtp.mailtrap.io";

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "false");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", "2525");

    // Get the Session object.
    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
          }
        });
    MimeMessage message = new MimeMessage(session);
    BodyPart messageBodyPart = new MimeBodyPart();
    Multipart multipart = new MimeMultipart();
    String filename = "./CargoMessage.xml";
    DataSource source = new FileDataSource(filename);
    InternetAddress eMailAddr = new InternetAddress();
    try {
      eMailAddr = new InternetAddress("baizeau.louis@gmail.com");
    } catch (AddressException aex) {
      aex.printStackTrace();
      System.out.println("Exception : Internet address parse failed : " + aex.toString());
    }

    try {
      // create mail
      message.setFrom(eMailAddr);
      message.setRecipient(Message.RecipientType.TO, eMailAddr);
      message.setSubject("Test");
      // create content
      messageBodyPart.setText("There is an xml file in attachement");
      multipart.addBodyPart(messageBodyPart);// add text
      messageBodyPart = new MimeBodyPart(); // TESTER SI FONCTIONNE SANS
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(filename);
      multipart.addBodyPart(messageBodyPart);// add attachement
      message.setContent(multipart);
      // Send mail
      Transport.send(message);
    } catch (MessagingException mex) {
      mex.printStackTrace();
      System.out.println(" Exception : Message not sent : " + mex.toString());
    }
    System.out.println("message sent successfully....");
  }

  public Optional<Move> getMove(final Long id) {
    return moveRepository.findById(id);
  }

  public void deleteMove(final Long id) {
    moveRepository.deleteById(id);
  }

  public void updateMove(Move move) {
    moveRepository.save(move);
  }

}