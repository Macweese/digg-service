package se.digg.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@NotBlank(message = "Name is required")
	private String name;

	@Column(nullable = false)
	@NotBlank(message = "Address is required")
	private String address;

	@Column(nullable = false, unique = true)
	@Email(message = "Email should be valid: prefix@domain.com")
	@NotBlank(message = "Email is required")
	private String email;

	@NotBlank(message = "Telephone is required")
	private String telephone;

	public User()
	{
	}

	public User(String name, String address, String email, String telephone)
	{
		this.name = name;
		this.address = address;
		this.email = email;
		this.telephone = telephone;
	}

	public User(Long id, String name, String address, String email, String telephone)
	{
		this.id = id;
		this.name = name;
		this.address = address;
		this.email = email;
		this.telephone = telephone;
	}

	@Override
	public String toString()
	{
		return "User{" +
			"id=" + id +
			", name='" + name + '\'' +
			", address='" + address + '\'' +
			", email='" + email + '\'' +
			", telephone='" + telephone + '\'' +
			'}';
	}
}