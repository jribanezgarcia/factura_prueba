ALTER TABLE cliente ADD COLUMN email TEXT NOT NULL DEFAULT '';
ALTER TABLE factura_version ADD COLUMN cli_email TEXT NOT NULL DEFAULT '';
ALTER TABLE factura_version ADD COLUMN forma_pago TEXT NOT NULL DEFAULT '';
ALTER TABLE factura_version ADD COLUMN vencimiento TEXT;
ALTER TABLE factura_version ADD COLUMN realizada_por TEXT NOT NULL DEFAULT '';
