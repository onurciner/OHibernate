# OHibernate
1.0.2

SQLite Connection for Android. ORM tool for Android devices.
First ORM with Geometric-Spatial data support for Android operating systems.

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
                compile 'com.github.10uroi:OHibernate:1.0.2'
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

			@Id(PRIMERY_KEY_AUTOINCREMENT = true)
			private Long id;

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
