package com.example.api.service;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.api.model.Move;
import com.example.api.repository.MoveRepository;

import java.io.File;
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
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
      move.setMoveDate(Instant.now().truncatedTo(ChronoUnit.MILLIS).toString());
      result = moveRepository.save(move);
      this.createXMLFile(move);
    }
    return result;
  }

  public List<Move> getMoves() {
    List<Move> moves = moveRepository.findFirst50ByOrderByCreationDateDesc();
    return moves;
  }

  private void createXMLFile(Move move) {
    try {

      DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      // ROOT
      Element cargoMessage = document.createElement("CargoMessage");
      cargoMessage.setAttribute("type", move.getInOut() ? "WarehouseMovement-Out" : "WarehouseMovement-In");
      document.appendChild(cargoMessage);
      // HEADER
      Element header = document.createElement("Header");
      header.setAttribute("from", "RAPIDCARGO");
      header.setAttribute("to", "CARGOINFO");
      header.setAttribute("messageTime", move.getCreationDate());
      header.setAttribute("messageId", move.getId().toString());
      cargoMessage.appendChild(header);
      // WarehouseMovementIn | WarehouseMovementOut
      Element warehouseMovement;
      if (!move.getInOut()) {
        warehouseMovement = document.createElement("WarehouseMovementIn");
      } else {
        warehouseMovement = document.createElement("WarehouseMovementOut");
      }
      cargoMessage.appendChild(warehouseMovement);

      // movementTime
      Element movementTime = document.createElement("movementTime");
      movementTime.appendChild(document.createTextNode(move.getMoveDate()));
      warehouseMovement.appendChild(movementTime);
      // declaredIn
      Element declaredIn = document.createElement("declaredIn");
      declaredIn.setAttribute("code", move.getDeclarationPlaceCode());
      declaredIn.setAttribute("label", move.getDeclarationPlace());
      warehouseMovement.appendChild(declaredIn);
      // From | To
      Element FromTo;
      if (!move.getInOut()) {
        FromTo = document.createElement("from");
      } else {
        FromTo = document.createElement("to");
      }
      FromTo.setAttribute("code", move.getWarehousCode());
      FromTo.setAttribute("label", move.getWarehousLabel());
      warehouseMovement.appendChild(FromTo);
      // goods
      Element goods = document.createElement("goods");
      warehouseMovement.appendChild(goods);
      // ref
      Element ref = document.createElement("ref");
      ref.setAttribute("type", move.getRefType());
      ref.setAttribute("code", move.getRef());
      goods.appendChild(ref);
      // amout
      Element amout = document.createElement("amout");
      amout.setAttribute("quantity", String.valueOf(move.getQuantity()));
      amout.setAttribute("weight", String.valueOf(move.getWeight()));
      goods.appendChild(amout);
      // description
      Element description = document.createElement("description");
      description.appendChild(document.createTextNode(move.getDescription()));
      goods.appendChild(description);
      // totalRefAmount
      Element totalRefAmount = document.createElement("totalRefAmount");
      totalRefAmount.setAttribute("quantity", String.valueOf(move.getTotalQuantity()));
      totalRefAmount.setAttribute("weight", String.valueOf(move.getTotalWeight()));
      goods.appendChild(totalRefAmount);
      // Customs Satus
      Element customsStatus = document.createElement("customsStatus");
      customsStatus.appendChild(document.createTextNode(move.getCustomsStatus()));
      warehouseMovement.appendChild(customsStatus);
      // IF OUT Customs Document
      if (move.getInOut()) {
        Element customsDocument = document.createElement("customsDocument");
        customsDocument.setAttribute("type", move.getCustomsDocType());
        customsDocument.setAttribute("ref", move.getCustomsDocRef());
        warehouseMovement.appendChild(customsDocument);
      }

      // create the xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      DOMSource domSource = new DOMSource(document);
      StreamResult streamResult = new StreamResult(new File("./CargoMessage.xml"));
      transformer.transform(domSource, streamResult);

      System.out.println("Done creating XML File");
      // this.sendMail();
    } catch (ParserConfigurationException pce) {
      pce.printStackTrace();
    } catch (TransformerException tfe) {
      tfe.printStackTrace();
    }
  }

  private void sendMail() {
    final String username = "*************";
    final String password = "***********";

    String host = "***************";// express-relay.jangosmtp.net

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", host);
    props.put("mail.smtp.port", "25");

    // Get the Session object.
    Session session = Session.getInstance(props,
        new javax.mail.Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
          }
        });

    try {
      // create mail
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress("baizeau.louis@gmail.com"));
      message.setRecipient(Message.RecipientType.TO,
          new InternetAddress("baizeau.louis@gmail.com"));
      message.setSubject("Test");
      // create content
      BodyPart messageBodyPart = new MimeBodyPart();
      messageBodyPart.setText("There is an xml file in attachement");
      Multipart multipart = new MimeMultipart();
      multipart.addBodyPart(messageBodyPart);// add text
      messageBodyPart = new MimeBodyPart();
      String filename = "./CargoMessage.xml";
      DataSource source = new FileDataSource(filename);
      messageBodyPart.setDataHandler(new DataHandler(source));
      messageBodyPart.setFileName(filename);
      multipart.addBodyPart(messageBodyPart);// add attachement
      message.setContent(multipart);
      // Send mail
      Transport.send(message);
      System.out.println("message sent successfully....");
    } catch (MessagingException mex) {
      mex.printStackTrace();
    }
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