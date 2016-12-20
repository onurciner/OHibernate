# OHibernate
1.0.8

SQLite Connection for Android. ORM tool for Android devices.
First ORM with Geometric-Spatial data support for Android operating systems.

There is relational table support. OneToOne and OneToMany are fully supported. 
OHQL support for simple inquiries.

#### Log
v1.0.8 -> Solved single quotes problem.

##Installation

To use the library, first include it your project using Gradle

        allprojects {
            repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
        }
and:

        dependencies {
                compile 'com.github.10uroi:OHibernate:1.0.8'
        }
##How to use

###Attached into the "MainActivity" class
		public class MainActivity extends AppCompatActivity {
		
		    @Override
			protected void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);

			OHibernateConfig.DB_PATH = dbPath; // DATABASE PATH
			OHibernateConfig.DB_NAME = dbName; // DATABASE NAME
			
			}
		}

###We are creating the "ExampleBean" class
        @Entity(TABLE_NAME = "example", TABLE_OPERATION = Entity.TABLE_OPERATION_TYPE.DROP_AND_CREATE)
		public class ExampleBean {

			@Id(PRIMARY_KEY_AUTOINCREMENT = true)
			private Integer id;

			private String name;

			private String surname;

			private int age;

			@Column(NAME = "active") //column customization
			private boolean status;

			@Column(DATETIME = true) //column customization
			private String datetime;
			
			//GETTER - SETTER
		}
		
###We are creating the "ExampleBeanDAO" class		
		public class ExampleBeanDAO {

			//Automatic Transactions
		
			OHibernate<ExampleBean> oHibernate = new OHibernate<>(ExampleBean.class);

			public void insert(ExampleBean exampleBean){
				try {
					oHibernate.insert(exampleBean); // Returns the id of the object
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
			}

			public void update(ExampleBean exampleBean){
				try {
					oHibernate.update(exampleBean);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
			}

			public void delete(ExampleBean exampleBean){
				try {
					oHibernate.delete(exampleBean);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
			}

			public ExampleBean select(Integer id){
				try {
					return (ExampleBean) oHibernate.where("id",id).select();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
				return null;
			}

			public ArrayList<ExampleBean> selectAll(){
				try {
					return (ArrayList<ExampleBean>) oHibernate.selectAll();
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
				return null;
			}

			public ArrayList<ExampleBean> selectAll(String surname){
				try {
					return (ArrayList<ExampleBean>) oHibernate.where("surname",surname).limit(5).selectAll(); // custom
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
				return null;
			}

			public ExampleBean selectCustom(String name,String surname){
				try {
					return (ExampleBean) oHibernate.where("name",name).and().where("surname",surname, LIKE_TYPE.BOTH).select(); // custom
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Error",e.getMessage());
				}
				return null;
			}
		}


## Relational tables
### Example OneToMany
<table style="width:100%; border-collapse: collapse;" >
  <tr>
    <th>User</th>
    <th>Address</th>
  </tr>
  <tr style="background: none">
    <td style="padding:0; margin:0; border:none; width:50%;">
      <pre lang="java"><code class="language-java">@Entity(TABLE_NAME = "users", TABLE_OPERATION = Entity.TABLE_OPERATION_TYPE.CREATE)
	public class User {

	  @Id(PRIMARY_KEY_AUTOINCREMENT = true)
	  private Integer id;

	  private String firstName;

	  private String lastName;

	  @OneToMany(JoinColumn = "user_id", Cascade = CascadeType.ALL, Fetch = FetchType.EAGER)
	  private ArrayList<Address> addresses;

	  ...
	  //Getter - Setter
	}
       </code>
     </pre>
    </td>
    <td style="padding:0; margin:0; border:none; width:50%;">
      <pre lang="java"><code class="language-java">@Entity(TABLE_NAME = "addresses",TABLE_OPERATION = Entity.TABLE_OPERATION_TYPE.CREATE)
	public class Address {

	  @Id(PRIMARY_KEY_AUTOINCREMENT = true)
	  private Integer id;

	  private String county;

	  @Column(NAME="phone_number")
	  private Long phoneNumber;

	  private Integer user_id;

	  ...
	  //Getter - Setter
	}
      </code></pre>
    </td>
  </tr>
</table>
### DB Tables
<table style="width:100%; border-collapse: collapse;" >
  <tr>
    <th colspan="3">users</th>
    <th colspan="1"></th>
    <th colspan="4">addresses</th>
  </tr>
  <tr style="background: none">
    <td style="padding:0; margin:0; border:none;">
      id
    </td>
    <td style="padding:0; margin:0; border:none;">
      firstname
    </td>
     <td style="padding:0; margin:0; border:none;">
      lastname
    </td>
    <td style="padding:0; margin:0; border:none;">
     
    </td>
    <td style="padding:0; margin:0; border:none;">
      id
    </td>
    <td style="padding:0; margin:0; border:none;">
      county
    </td>
    <td style="padding:0; margin:0; border:none;">
      phone_number
    </td>
    <td style="padding:0; margin:0; border:none;">
      user_id
    </td>
  </tr>
  <tr style="background: none">
    <td style="padding:0; margin:0; border:none;">
      1
    </td>
    <td style="padding:0; margin:0; border:none;">
      Onur
    </td>
     <td style="padding:0; margin:0; border:none;">
      Ciner
    </td>
    <td style="padding:0; margin:0; border:none;">
     ┬►
    </td>
    <td style="padding:0; margin:0; border:none;">
      1
    </td>
    <td style="padding:0; margin:0; border:none;">
      Ankara
    </td>
    <td style="padding:0; margin:0; border:none;">
      05554443322
    </td>
    <td style="padding:0; margin:0; border:none;">
      1
    </td>
  </tr>
   <tr style="background: none">
    <td style="padding:0; margin:0; border:none;">
     
    </td>
    <td style="padding:0; margin:0; border:none;">
     
    </td>
     <td style="padding:0; margin:0; border:none;">
    
    </td>
    <td style="padding:0; margin:0; border:none;">
     └►
    </td>
    <td style="padding:0; margin:0; border:none;">
      2
    </td>
    <td style="padding:0; margin:0; border:none;">
      İstanbul
    </td>
    <td style="padding:0; margin:0; border:none;">
      05554443311
    </td>
    <td style="padding:0; margin:0; border:none;">
      1
    </td>
  </tr>
</table>

## OHQL (The OHibernate Query Language)
### Single Select
	User user = (User) new OQuery()
		.addEntity(User.class)		//=>Returns a String if entity is not added
		.Select("*")
		.From("users")				//=> "users"->table name
		.Where("id",2)
		.getSingleResult();			//=> Fetch user with id 2 in the users table
### List Select
	ArrayList<User> users = new OQuery()
		.addEntity(User.class)		//=>Returns a String if entity is not added
		.Select("*")
		.From("users")				//=> "users"->table name
		.list();					//=> Brings all users in the users table
### Insert Query
	new OQuery()
		.SetParameter("firstName","Onur")
		.SetParameter("lastName","Ciner")
		.Insert("users");			//=> "users"->table name
### Insert Entity Query
	Users user = new Users();					//=>The object is created
	user.setFirstName("Onur");
	user.setLastName("Ciner");
	new OQuery().InsertEntity("users",user);	//=> "users"->table name
### Update Query
	new OQuery()
		.SetParameter("firstName", "Selçuk")
		.SetParameter("lastName", "Uzunsoy")
		.Where("id", 15)				//=> User with id 15 will be updated
		.Update("users"); 				//=> "users"->table name
### Update Entity Query
	Users user = OQuery().Select...;			//=> Object brought
	user.setFirstName("Onur");
	user.setLastName("Ciner");
	new OQuery().UpdateEntity("users",user);	//=> "users"->table name
### Delete Query
	new OQuery()
		.Where("id",15)		//=> User with id 15 will be deleted
		.Delete("users");	//=> "users"->table name
### DeleteAll Query
	new OQuery()
		.DeleteAll("users");	//=> All users in the users table will be deleted
