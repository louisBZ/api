DROP TABLE IF EXISTS moves;
 
CREATE TABLE moves (
  id INT AUTO_INCREMENT  PRIMARY KEY,
  in_out BOOLEAN NOT NULL,
  creation_date DATE NOT NULL,
  creation_user VARCHAR(250) NOT NULL,
  move_date DATE NOT NULL,
  declaration_place VARCHAR(250) NOT NULL,
  declaration_place_code VARCHAR(250) NOT NULL,
  warehouse_code INT NOT NULL,
  warehouse_label VARCHAR(250) NOT NULL,
  customs_status VARCHAR(250) NOT NULL,
  customs_doc_type VARCHAR(250),
  customs_doc_ref  VARCHAR(250),
  ref_type VARCHAR(250) NOT NULL,
  ref VARCHAR(250) NOT NULL,
  quantity INT NOT NULL,
  weight INT NOT NULL,
  total_weight INT NOT NULL,
  total_quantity INT NOT NULL,
  description VARCHAR(250) NOT NULL
);
