package se.digg.application.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User
{
	private UUID id = UUID.randomUUID();

	@NotBlank(message = "Name is required")
	private String name;

	@NotBlank(message = "Address is required")
	private String address;

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
		this.id = UUID.randomUUID();
		this.name = name;
		this.address = address;
		this.email = email;
		this.telephone = telephone;
	}

	public User(UUID id, String name, String address, String email, String telephone)
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