/**
 * Diego Moreno Lennon
 * Burger Javi's Server
 */

package com.burgerjavis.mvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.burgerjavis.BurgerJavisServerApplication;
import com.burgerjavis.Common;
import com.burgerjavis.MongoTestConfiguration;
import com.burgerjavis.entities.User;
import com.burgerjavis.repositories.UserRepository;
import com.burgerjavis.util.TestDatabaseLoader;

import io.github.bonigarcia.wdm.ChromeDriverManager;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={MongoTestConfiguration.class})
@WebAppConfiguration
public class BurgerJavisMVCUserTest {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TestDatabaseLoader dbLoad;
	
	private WebDriver driver;
	private static ApplicationContext context;
	private StringBuffer verificationErrors = new StringBuffer();
	
	@BeforeClass public static void setupClass() {
		ChromeDriverManager.getInstance().setup();
	}
	
	@Before
	public void setUp() throws Exception {
		dbLoad.initDatabase();
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		context = (ApplicationContext) SpringApplication.run(BurgerJavisServerApplication.class);
	}
	
	@Test
	public void testBurgerJavisMVCUserAdd() throws Exception {
		// Login
		driver.get("http://localhost:12345/");
		driver.findElement(By.name("username")).click();
		driver.findElement(By.name("username")).clear();
		driver.findElement(By.name("username")).sendKeys("admin");
		driver.findElement(By.name("password")).click();
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys("admin");
		driver.findElement(By.xpath("//input[@value='Iniciar sesión']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/"));
		assertTrue(driver.getTitle().equalsIgnoreCase("Burger Javi's - Inicio"));
		
		// Create new user
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("(//a[contains(text(),'+')])[4]")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("NuevoUsuario");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.id("role1")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertNotNull(userRepository.findByUsernameIgnoreCase("NuevoUsuario"));
		
		// User with name in use
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("(//a[contains(text(),'+')])[4]")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("NuevoUsuario");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.id("role1")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("NAME_IN_USE"));
		
		// User with empty name
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys(" ");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.id("role1")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
		
		// User with invalid name
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("Nuevo usuario"); // No spaces allowed
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.id("role1")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
		assertNull(userRepository.findByUsernameIgnoreCase("Nuevo usuario"));
		
		// User with invalid password
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("NuevoUsuario2");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pas");
		driver.findElement(By.id("role1")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
		assertNull(userRepository.findByUsernameIgnoreCase("NuevoUsuario2"));
	}
	
	@Test
	public void testBurgerJavisMVCUserModify() throws Exception {
		// Login
		driver.get("http://localhost:12345/");
		driver.findElement(By.name("username")).click();
		driver.findElement(By.name("username")).clear();
		driver.findElement(By.name("username")).sendKeys("admin");
		driver.findElement(By.name("password")).click();
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys("admin");
		driver.findElement(By.xpath("//input[@value='Iniciar sesión']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/"));
		assertTrue(driver.getTitle().equalsIgnoreCase("Burger Javi's - Inicio"));
		
		// Modify user data
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[2]/span")).click();
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("ModifiedUser");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("newPass");
		driver.findElement(By.id("role2")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		User user = userRepository.findByUsernameIgnoreCase("ModifiedUser");
		assertNotNull(user);
		assertTrue(user.getRoles().get(0).getAuthority().equalsIgnoreCase(Common.WAITER_ROLE));
		assertTrue(new BCryptPasswordEncoder().matches("newPass", user.getPassword()));
		
		// Minimum number of admins
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[3]/span")).click();
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("Admin");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("admin");
		driver.findElement(By.id("role2")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("MIN_ADMINS"));
		
		// User with empty username
		driver.get("http://localhost:12345/");
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[2]/span")).click();
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys(" ");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
		
		// User with invalid name
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("Modified user");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
		
		// User with name in use
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("admin");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("admin");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("NAME_IN_USE"));
		
		// User with invalid password
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("ModifiedUser");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pas");
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("INVALID_DATA"));
	}
	
	@Test
	public void testBurgerJavisMVCUserDelete() throws Exception {
		// Login
		driver.get("http://localhost:12345/webclient/login");
		driver.findElement(By.name("username")).click();
		driver.findElement(By.name("username")).clear();
		driver.findElement(By.name("username")).sendKeys("admin");
		driver.findElement(By.name("password")).click();
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys("admin");
		driver.findElement(By.xpath("//input[@value='Iniciar sesión']")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/"));
		assertTrue(driver.getTitle().equalsIgnoreCase("Burger Javi's - Inicio"));
		
		// Delete user
		int nUsers = ((List<User>) userRepository.findAll()).size();
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[2]")).click();
		driver.findElement(By.xpath("(//button[@type='submit'])[2]")).click();
		assertEquals(userRepository.findByUsernameIgnoreCase("user2"), null);
		assertEquals(((List<User>) userRepository.findAll()).size(), nUsers - 1);
		
		// Create new admin user
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("(//a[contains(text(),'+')])[4]")).click();
		assertTrue(driver.getCurrentUrl().equalsIgnoreCase("http://localhost:12345/webclient/user/add"));
		driver.findElement(By.id("inputName")).click();
		driver.findElement(By.id("inputName")).clear();
		driver.findElement(By.id("inputName")).sendKeys("user2");
		driver.findElement(By.id("inputPassword")).click();
		driver.findElement(By.id("inputPassword")).clear();
		driver.findElement(By.id("inputPassword")).sendKeys("pass");
		driver.findElement(By.id("role3")).click();
		driver.findElement(By.xpath("//button[@type='submit']")).click();
		assertNotNull(userRepository.findByUsernameIgnoreCase("user2"));
		assertEquals(((List<User>) userRepository.findAll()).size(), nUsers);
		
		// Delete admin user
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[3]/span")).click();
		driver.findElement(By.xpath("(//button[@type='submit'])[2]")).click();
		assertEquals(userRepository.findByUsernameIgnoreCase("user2"), null);
		assertEquals(((List<User>) userRepository.findAll()).size(), nUsers - 1);
		
		// Minimum admin users
		driver.findElement(By.linkText("Usuarios")).click();
		TimeUnit.SECONDS.sleep(1);
		driver.findElement(By.xpath("//div[@id='users']/div/div/a[2]/span")).click();
		driver.findElement(By.xpath("(//button[@type='submit'])[2]")).click();
		assertEquals(userRepository.findByUsernameIgnoreCase("user2"), null);
		assertEquals(((List<User>) userRepository.findAll()).size(), nUsers - 1);
		assertTrue(driver.findElement(By.xpath("//div/span")).getText().equalsIgnoreCase("MIN_ADMINS"));
	}
	
	@After
	public void tearDown() throws Exception {
		driver.quit();
		SpringApplication.exit(context);
		String verificationErrorString = verificationErrors.toString();
		if (!"".equals(verificationErrorString)) {
			fail(verificationErrorString);
		}
	}
	
	@AfterClass public static void tearDownClass() {
	}

}
