package org.hibernate.bugs;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLInsert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class EmbeddedIdTest {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	// Entities are auto-discovered, so just add them anywhere on class-path
	// Add your tests, using standard JUnit.
	@Test
	public void HHH16849() throws Exception {
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		// Do stuff...

		doInJPA( ()->entityManagerFactory, em -> {
			CompositeKey key = new CompositeKey();
			key.setAccountId(1679911196L);
			key.setId(3L);

			SalesContact salesContact = em.find(SalesContact.class, key);
			System.out.println(salesContact.getKey().getAccountId());

			SalesContact contact = new SalesContact();
			CompositeKey insertKey = new CompositeKey();
			insertKey.setAccountId(1679911196L);
			contact.setFirstName("John");
			contact.setLastName("Doe");
			contact.setKey(insertKey);

			ContactCustomField2 contactCustomField2 = new ContactCustomField2();
			contactCustomField2.setKey(insertKey);
			contact.setContactCustomField2(contactCustomField2);

			ContactCustomField contactCustomField = new ContactCustomField();
			contactCustomField.setKey(insertKey);
			contact.setContactCustomField(contactCustomField);

			entityManager.persist( contact );

			Assertions.assertNotNull(key.getId());
		});

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	@Embeddable
	public static class CompositeKey implements Serializable {
		@Column(name = "account_id")
		private Long accountId;

		@Column(name = "id")
		//@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		public CompositeKey(Long accountId, Long id){
			this.accountId = accountId;
			this.id = id;
		}

		public CompositeKey(){

		}

		public Long getAccountId() {
			return accountId;
		}

		public void setAccountId(Long accountId) {
			this.accountId = accountId;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}
	}

	@Table(name = "contacts")
	@Entity
	@SQLInsert(
		sql = "insert into contacts (created_at, first_name, last_name, updated_at, account_id, id)  values (?, ?, ?, ?, ?, ?)"
	)
	public static class SalesContact
		implements Serializable {

		@EmbeddedId
		private CompositeKey key;

		@Column(name = "first_name")
		private String firstName;

		@Column(name = "last_name")
		private String lastName;

		@Column(name = "created_at")
		private Instant createdAt;

		@Column(name = "updated_at")
		private Instant updatedAt;

		public String getFirstName() {
			return firstName;
		}

		public SalesContact(){
			this.createdAt = Instant.now();
			this.updatedAt = Instant.now();
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public CompositeKey getKey() {
			return key;
		}

		public void setKey(CompositeKey key) {
			this.key = key;
		}

		@OneToOne(mappedBy = "contact", cascade = CascadeType.ALL)
		private ContactCustomField contactCustomFields = new ContactCustomField();

		@OneToOne(mappedBy = "contact", cascade = CascadeType.ALL)
		private ContactCustomField2 contactCustomField2 = new ContactCustomField2();

		public ContactCustomField getContactCustomFields(){
			return contactCustomFields;
		}

		public ContactCustomField2 getContactCustomField2(){
			return contactCustomField2;
		}
		public void setContactCustomField2(ContactCustomField2 contactCustomField2){
			this.contactCustomField2=contactCustomField2;
			contactCustomField2.setContact(this);
		}
		public void setContactCustomField(ContactCustomField contactCustomField){
			this.contactCustomFields=contactCustomField;
			contactCustomFields.setContact(this);
		}
	}

	@Table(name = "contact_custom_fields")
	@Entity
	public static class ContactCustomField{
		@EmbeddedId
		private CompositeKey key;

		public ContactCustomField(){

		}

		public void setKey(CompositeKey key) {
			this.key = key;
		}

		@OneToOne(fetch = FetchType.LAZY)
		@MapsId
		@JoinColumn(name = "contact_id", referencedColumnName = "id")
		@JoinColumn(name = "account_id", referencedColumnName = "account_id")
		private SalesContact contact;

		public CompositeKey getId(){
			return key;
		}

		public void setContact(SalesContact contact){
			this.contact=contact;
		}

	}

	@Table(name = "contact_custom_field2")
	@Entity
	public static class ContactCustomField2{
		@EmbeddedId
		private CompositeKey key;

		public ContactCustomField2(){

		}

		public void setKey(CompositeKey key) {
			this.key = key;
		}

		@OneToOne(fetch = FetchType.LAZY)
		@MapsId
		@JoinColumn(name = "contact_id", referencedColumnName = "id")
		@JoinColumn(name = "account_id", referencedColumnName = "account_id")
		private SalesContact contact;

		public CompositeKey getId(){
			return key;
		}

		public void setContact(SalesContact contact){
			this.contact=contact;
		}
	}
}
