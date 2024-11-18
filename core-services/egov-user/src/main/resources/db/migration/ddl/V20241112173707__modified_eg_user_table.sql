ALTER TABLE eg_user
    ADD COLUMN hashedusername character varying(128),
    ADD COLUMN hashedname character varying(128),
    ADD COLUMN hashedmobilenumber character varying(128),
    ADD COLUMN hashedemailid character varying(128);
