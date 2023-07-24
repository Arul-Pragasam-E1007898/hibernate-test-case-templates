package org.hibernate.bugs;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Persistence;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.annotations.PartitionKey;
import org.hibernate.bugs.entities.SalesContact;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class PartitionKey1_1Test {

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
			SalesContact salesContact = findByIdAndAccountId(em, 3L, 1679911196L);
			//SalesContact salesContact = em.find(SalesContact.class, 3);
			System.out.println(salesContact.getAccountId());
			System.out.println(salesContact.getContactCustomFields().getId());
			System.out.println(salesContact.getContactCustomField2().getId());
		});

		entityManager.getTransaction().commit();
		entityManager.close();
	}

	public static SalesContact findByIdAndAccountId(EntityManager em, Long id, Long accountId) {
		// Assuming 'em' is your EntityManager instance
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<SalesContact> query = cb.createQuery(SalesContact.class);
		Root<SalesContact> root = query.from(SalesContact.class);

		// Define the predicates for the two attributes
		Predicate idPredicate = cb.equal(root.get("id"), id);
		Predicate accountIdPredicate = cb.equal(root.get("accountId"), accountId);

		// Combine the predicates using 'and' to form the final condition
		Predicate finalPredicate = cb.and(idPredicate, accountIdPredicate);

		// Add the final condition to the query
		query.where(finalPredicate);

		// Execute the query and return the result (if found)
		return em.createQuery(query).getSingleResult();
	}

	@Table(name = "contact_custom_fields")
	@Entity
	public static class ContactCustomField{
		@Id
		@Column(name = "id")
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		public ContactCustomField(){

		}

		@OneToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "contact_id", referencedColumnName = "id", nullable = false)
		@JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false)
		private SalesContact contact;

		public Long getId(){
			return id;
		}

	}

	@Table(name = "contact_custom_field2")
	@Entity
	public static class ContactCustomField2{
		@Id
		@Column(name = "id")
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		public ContactCustomField2(){

		}

		@OneToOne(fetch = FetchType.LAZY)
		@JoinColumn(name = "contact_id", referencedColumnName = "id", nullable = false)
		@JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false)
		private SalesContact contact;

		public Long getId(){
			return id;
		}

	}


	@Table(name = "contacts")
	@Entity
	public static class SalesContact
		implements Serializable {

		public SalesContact(){}

		@Column(name = "first_name")
		private String firstName;

		@Column(name = "last_name")
		private String lastName;

		@PartitionKey
		@Column(name = "account_id", nullable = false)
		private Long accountId;

		@Id
		@Column(name = "id")
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

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

		public String getFirstName() {
			return firstName;
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
}