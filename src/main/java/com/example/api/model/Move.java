package com.example.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.function.Function;

@Data
@Entity
@Table(name = "moves")
public class Move {

  public Move() {
  }

  public Move(Instant creationDate, String creationUser, Instant moveDate,
      String declarationPlace, String declarationPlaceCode, String warehousCode, String warehousLabel,
      String customsStatus, String refType, String ref, int quantity, int weight, int totalQuantity, int totalWeight,
      String description) {
    this.inOut = false;
    this.creationDate = creationDate;
    this.creationUser = creationUser;
    this.moveDate = moveDate;
    this.declarationPlace = declarationPlace;
    this.declarationPlaceCode = declarationPlaceCode;
    this.warehousCode = warehousCode;
    this.warehousLabel = warehousLabel;
    this.customsStatus = customsStatus;
    this.refType = refType;
    this.ref = ref;
    this.quantity = quantity;
    this.weight = weight;
    this.totalQuantity = totalQuantity;
    this.totalWeight = totalWeight;
    this.description = description;
  }

  public Move(String customsDocType, String customsDocRef, Instant creationDate,
      String creationUser, Instant moveDate, String declarationPlace, String declarationPlaceCode, String warehousCode,
      String warehousLabel, String customsStatus, String refType, String ref, int quantity, int weight,
      int totalQuantity, int totalWeight, String description) {
    this.inOut = true;
    this.creationDate = creationDate;
    this.creationUser = creationUser;
    this.moveDate = moveDate;
    this.declarationPlace = declarationPlace;
    this.declarationPlaceCode = declarationPlaceCode;
    this.warehousCode = warehousCode;
    this.warehousLabel = warehousLabel;
    this.customsStatus = customsStatus;
    this.refType = refType;
    this.ref = ref;
    this.quantity = quantity;
    this.weight = weight;
    this.totalQuantity = totalQuantity;
    this.totalWeight = totalWeight;
    this.description = description;
    this.customsDocType = customsDocType;
    this.customsDocRef = customsDocRef;
  }

  public Function<Object, Object> messageType() {
    return (obj) -> this.inOut ? String.format("WarehouseMovement-Out", obj)
        : String.format("WarehouseMovement-In", obj);
  }

  public Function<Object, Object> fromTo() {
    return (obj) -> this.inOut ? String.format("to", obj)
        : String.format("from", obj);
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "in_out")
  private Boolean inOut;

  @Column(name = "creation_date")
  private Instant creationDate;

  @Column(name = "creation_user")
  private String creationUser;

  @Column(name = "move_date")
  private Instant moveDate;

  @Column(name = "declaration_place")
  private String declarationPlace = "RapidCargo CDG";

  @Column(name = "declaration_place_code")
  private String declarationPlaceCode = "CDGRC1";

  //////// different use for In and OUT
  @Column(name = "warehouse_code")
  private String warehousCode;

  @Column(name = "warehouse_label")
  private String warehousLabel;
  /////////

  @Column(name = "customs_status")
  private String customsStatus;

  // only for Out Move
  @Column(name = "customs_doc_type")
  private String customsDocType = null;

  @Column(name = "customs_doc_ref")
  private String customsDocRef = null;
  ///////////////////////

  @Column(name = "ref_type")
  private String refType;

  private String ref;

  private int quantity;

  private int weight;

  @Column(name = "total_quantity")
  private int totalQuantity;

  @Column(name = "total_weight")
  private int totalWeight;

  private String description;

}