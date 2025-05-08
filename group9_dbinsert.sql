CREATE TABLE STUDENTS( SID INTEGER, GENDER CHAR(6), NAME CHAR(20),MAJOR CHAR(20),DISCOUNT_LEVEL INTEGER,Primary KEY(SID));
CREATE TABLE ORDER_PLACED(SID INTEGER NOT NULL,order_price Integer,paymethod char(20),oid integer,odate date, card_no integer,primary key(oid), Foreign key(sid) references students);

CREATE TABLE Books(bid INTEGER, TITLE char(20), quantity integer, author char(20), price integer,Primary key(bid));

CREATE TABLE BookOrder_Hv(deliverydate date,boid integer,quantity integer,oid integer not null, sid integer not null,bid integer not null, primary key(boid),foreign key(oid) references order_placed on delete cascade,foreign key(bid) references books);
create table hv(bid integer,boid integer not null,primary key(boid),foreign key(bid) references books, foreign key(boid) references bookorder_hv on delete cascade);

CREATE TRIGGER order_placed_trigger
BEFORE INSERT ON bookorder_hv
FOR EACH ROW
DECLARE
book_quantity INTEGER;
BEGIN
SELECT quantity
INTO book_quantity
FROM books
WHERE bid =:new.bid;
 IF book_quantity <= 0 or book_quantity<:new.quantity THEN
    RAISE_APPLICATION_ERROR(-20001, 'One or more books in the order are out of stock.');
END IF;
end;

create trigger out_standing_trigger
BEFORE INSERT ON order_placed
FOR EACH ROW
DECLARE
outstanding_orders INTEGER;
begin


SELECT COUNT(*)
INTO outstanding_orders
FROM bookorder_hv
WHERE sid = :NEW.sid
AND deliverydate IS not NULL;

IF outstanding_orders > 0 THEN
    RAISE_APPLICATION_ERROR(-20002, 'The student has outstanding orders.');
END IF;
end;

CREATE OR REPLACE TRIGGER successinsert
     AFTER INSERT ON bookorder_hv
     FOR EACH ROW
     DECLARE
            c INTEGER;
            d integer;
     BEGIN
       SELECT SUM(order_price) into c FROM order_placed
            WHERE sid = :new.sid;
       select quantity into d from books where bid=:new.bid;
       UPDATE BOOKS SET QUANTITY=d-:new.quantity WHERE bid=:new.bid;
       IF (c>2000) THEN
             UPDATE Students SET DISCOUNT_LEVEL=20 WHERE sid = :new.sid;
                   ELSIF (c>1000) THEN
             UPDATE Students SET DISCOUNT_LEVEL=10 WHERE sid = :new.sid;
       END IF;
     END;

CREATE OR REPLACE TRIGGER successdelete
     AFTER DELETE ON bookorder_hv
     FOR EACH ROW
     DECLARE
            c INTEGER;
            d integer;
e integer;
     BEGIN
select price into e from books where bid=:old.bid;
       SELECT SUM(order_price)-e*:old.quantity into c FROM order_placed
            WHERE sid = :old.sid;
       select quantity into d from books where bid=:old.bid;
       UPDATE BOOKS SET QUANTITY= d+:old.quantity WHERE bid=:old.bid;
       IF (c<1000) THEN
             UPDATE Students SET DISCOUNT_LEVEL=0 WHERE sid = :old.sid;
       ELSIF (c<2000) THEN
             UPDATE Students SET DISCOUNT_LEVEL=10 WHERE sid = :old.sid;
       END IF;
     END;

CREATE or replace TRIGGER CARD_CONSTRAINT
BEFORE INSERT OR UPDATE ON ORDER_PLACED
FOR EACH ROW
BEGIN
IF(:NEW.paymethod = 'creditcard' AND :NEW.card_no = 0)
THEN
RAISE_APPLICATION_ERROR(-20010,  'INVALID CARD ');
END IF;
END;
CREATE TRIGGER totalsum_trigger
BEFORE INSERT ON bookorder_hv
FOR EACH ROW
DECLARE
    total_price INTEGER;
    sum1 INTEGER;
    dis INTEGER;
    pri INTEGER;
BEGIN
    SELECT discount_level INTO dis FROM students WHERE sid= :new.sid;
    SELECT price INTO pri FROM books WHERE bid=:new.bid;
    SELECT order_price INTO total_price FROM order_placed WHERE oid= :new.oid;
    sum1 := (:new.quantity * pri * (1 - (dis/ 100)));
    UPDATE order_placed SET order_price = total_price + sum1 WHERE oid = :new.oid;
END;

CREATE OR REPLACE TRIGGER check_order_cancellation
BEFORE DELETE ON ORDER_PLACED
for each row
DECLARE 
    delivered_books INT;
BEGIN
    SELECT COUNT(boid) INTO delivered_books
    FROM BookOrder_Hv
    WHERE oid = :OLD.oid AND deliverydate IS NULL;

    IF delivered_books > 0 THEN
        RAISE_APPLICATION_ERROR (-20012, 'Cannot cancel order: Some books have been delivered.');
    END IF;
end;
CREATE OR REPLACE TRIGGER day7
BEFORE DELETE ON ORDER_PLACED
FOR EACH ROW
DECLARE 
    delivered_books INT;
    order_age INT;
BEGIN
    SELECT (TRUNC(SYSDATE) - TRUNC(:OLD.odate)) INTO order_age
    FROM dual;
    IF order_age > 7 THEN
        RAISE_APPLICATION_ERROR (-20011, 'Cannot cancel order: Order was made more than 7 days ago.');
    END IF;
END;


INSERT INTO STUDENTS VALUES(190,  'MALE ', 'KURT ', 'CS ',10)
INSERT INTO STUDENTS VALUES(200, 'MALE ', 'REX ', 'ART ',0);
INSERT INTO STUDENTS VALUES(210, 'MALE ', 'JERRY ', 'BIO ',0);
insert into books valueS(1, 'Harry Potter I ',11, 'JK ',300);
INSERT INTO BOOKS VALUES(2, 'HARRY POTTER II ',2, 'JK ',300);
INSERT INTO BOOKS VALUES(3, 'HARRY POTTER III ',20, 'JK ',400);
INSERT INTO ORDER_PLACED VALUES(190,1600, 'CASH ',1, '11-MAR-22',null);
INSERT INTO ORDER_PLACED VALUES(200,300, 'CASH ',2, '10-MAR-22',NULL);
INSERT INTO ORDER_PLACED VALUES(210,300, 'CASH ',3, '21-MAR-22',NULL);
INSERT INTO ORDER_PLACED VALUES(200,0,'CASH',4,'17-APR-23',NULL);
INSERT INTO bookorder_hv values(null,99,1,4,200,1);
INSERT INTO bookorder_hv values('17-APR-23',100,1,4,200,2);

INSERT INTO ORDER_PLACED VALUES(190,0,'CASH',5,'10-APR-23',NULL);
INSERT INTO bookorder_hv values('17-APR-23',101,1,5,190,2);

//The following statement is do in java 
sid,order_price , pay method, oid, odate, card no,deliverydate,quantity,Bid,boid:
deliverydate,quantity,oid,sid,bid,boid
190,0,CASH,6,2023-04-15,null,2023-04-17,1,1,1

5

4

OUT OF STOCK
210,0,CASH,7,2023-04-15,NULL,2023-04-17,1,2,2

210,0,CASH,8,2023-04-15,NULL,2023-04-17,1,1,3

UPDATE AND CALL 200

UPDATE AND CALL 190

200,0,BankTransfer,9,2023-04-16,null,2023-04-17,1,1,4

190,0,creditcard,10,2023-04-16,213,2023-04-17,1,1,5
2023-04-17,1,10,190,3,6

10

call updates 200

200,0,creditcard,11,2023-04-20,213,2023-04-21,1,1,7
2023-04-21,1,11,200,3,8

search 200






