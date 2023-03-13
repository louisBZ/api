package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.api.exception.MoveBuisnessRulesException;
import com.example.api.model.Move;
import com.example.api.repository.MoveRepository;
import com.example.api.exception.MoveEmailException;
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
import javax.activation.FileDataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.Data;

@Data
@Service
public class MoveService {

  @Autowired
  private MoveRepository moveRepository;

  public Move saveMove(Move move) throws ResponseStatusException {
    move.setMoveDate(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    try {
      this.isRefValid(move.getRef(), move.getRefType(), move.getInOut());
      this.isQuantityWeightValid(move.getQuantity(), move.getWeight(), move.getTotalQuantity(),
          move.getTotalWeight());
    } catch (MoveBuisnessRulesException ex) {
      System.out.println(
          "MoveService Error on saveMove(move) Buisness rules not respected : " + ex.getMessage());
      // ex.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "This move dont respect all buisness rules : " + ex.getMessage());
    }
    try {
      this.createXMLFile(move);
      this.sendMail();
    } catch (MoveEmailException ex) {
      System.out.println(
          "MoveService Error on saveMove(move) Email not sent : " + ex.getMessage());
      // ex.printStackTrace();
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Email not sent : " + ex.getMessage());
    }
    return moveRepository.save(move);
  }

  private void isQuantityWeightValid(int quantity, int weight, int totalQuantity, int totalWeight)
      throws MoveBuisnessRulesException {
    if (totalQuantity >= quantity && totalWeight >= weight) {
      System.out.println("Quantity and weight is Valid.");
      return;
    } else {
      throw new MoveBuisnessRulesException(
          "isQuantityWeightValid() : Total quantity and total weight must be superior or equal to quantity and weight.");
    }
  }

  private void isRefValid(String ref, String refType, Boolean inOut) throws MoveBuisnessRulesException {
    if (!refType.equals("AWB") || ref.length() == 11) {
      List<Move> refExist = moveRepository.findByRef(ref);
      if (refExist.size() > 0 && !inOut) {
        throw new MoveBuisnessRulesException(
            "isRefValid() : An entry with this reference already exists.");
      } else if (refExist.size() == 0 && inOut) {
        throw new MoveBuisnessRulesException(
            "isRefValid() : There is no entry with this reference.");
      } else if (refExist.size() > 1 && inOut) {
        throw new MoveBuisnessRulesException(
            "isRefValid() : An output with this reference already exists.");
      } else {
        System.out.println("Ref is Valid");
        return;
      }
    } else {
      throw new MoveBuisnessRulesException(
          "isRefValid() : Ref type is AWB so ref must have 11 characters.");
    }
  }

  private void createXMLFile(Move move) throws MoveEmailException {
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
      fw.write(writer.toString());
      /*
       * // Unique template
       * fw.write(writer.toString().replaceAll("(?m)^[ \t]*\r?\n", ""));
       * // (?m) pour string avec plusieur lignes
       */
      fw.close();
    } catch (IOException ioex) {
      throw new MoveEmailException(
          "createXMLFile() : IOException : XML file not created : " + ioex.getMessage());
    }
    System.out.println("XML File created.");
  }

    final String username = "*************";
    final String password = "***********";
    String host = "sandbox.smtp.mailtrap.io";
  private void sendMail() throws MoveEmailException {
    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "false");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", "2525");
    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
          }
        });
    MimeMessage message = new MimeMessage(session);
    BodyPart attachmentMessageBodyPart = new MimeBodyPart();
    BodyPart textMessageBodyPart = new MimeBodyPart();
    Multipart multipart = new MimeMultipart();
    String filename = "./CargoMessage.xml";
    DataHandler source = new DataHandler(new FileDataSource(filename));
    InternetAddress eMailAddr = new InternetAddress();
    try {
      eMailAddr = new InternetAddress("baizeau.louis@gmail.com");
    } catch (AddressException aex) {
      throw new MoveEmailException(
          "sendMail() : AddressException : Internet address parse failed : " + aex.getMessage());
    }
    try {
      message.setFrom(eMailAddr);
      message.setRecipient(Message.RecipientType.TO, eMailAddr);
      message.setSubject("Test");
      textMessageBodyPart.setText("There is an xml file in attachement");
      multipart.addBodyPart(textMessageBodyPart);
      attachmentMessageBodyPart.setDataHandler(source);
      attachmentMessageBodyPart.setFileName(filename);
      multipart.addBodyPart(attachmentMessageBodyPart);
      message.setContent(multipart);
      Transport.send(message);
    } catch (MessagingException mex) {
      throw new MoveEmailException(
          "sendMail() : MessagingException : " + mex.getMessage());
    }
    System.out.println("message sent successfully.");
  }

  public List<Move> getMoves() throws ResponseStatusException {
    List<Move> moves = moveRepository.findFirst50ByOrderByCreationDateDesc();
    if (moves.size() > 0) {
      return moves;
    } else {
      System.out.println("MoveService Error on getMoves() There is no move saved.");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "There is no move saved.");
    }
  }

  public Move getMove(final Long id) throws ResponseStatusException {
    Optional<Move> move = moveRepository.findById(id);
    if (move.isPresent()) {
      return move.get();
    } else {
      System.out.println("MoveService Error on getMove(id) This move doesn't exist.");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "This move doesn't exist.");
    }
  }

  public void deleteMove(final Long id) throws ResponseStatusException {
    Optional<Move> e = moveRepository.findById(id);
    if (e.isPresent()) {
      moveRepository.deleteById(id);
    } else {
      System.out.println("MoveService Error on deleteMove(id) This move doesn't exist.");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot delete this move doesn't exist.");
    }
  }

  public Move updateMove(Long id, Move move) throws ResponseStatusException {
    Optional<Move> e = moveRepository.findById(id);
    if (e.isPresent()) {
      Move currentMove = e.get();

      String description = move.getDescription();
      if (description != null) {
        currentMove.setDescription(description);
      }
      String customsStatus = move.getCustomsStatus();
      if (customsStatus != null) {
        currentMove.setCustomsStatus(customsStatus);
      }
      return moveRepository.save(currentMove);
    } else {
      System.out.println("MoveService Error on updateMove(id, move) This move doesn't exist.");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cannot update this move doesn't exist.");
    }
  }
}