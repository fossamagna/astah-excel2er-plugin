package excel2er.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.exception.InvalidEditingException;
import com.change_vision.jude.api.inf.model.IERDomain;

import excel2er.AstahModelManager;
import excel2er.exceptions.ApplicationException;
import excel2er.models.Domain;
import excel2er.services.finder.DomainFinder;

public class ImportERDomainServiceTest {

	@Rule
	public TestName testName = new TestName();

	private boolean debug = true;

    private DomainFinder domainFinder = new DomainFinder();

    private ImportERDomainService sut = null;

    @Before
    public void before() throws Exception {
        sut = new ImportERDomainService();
    }

	@After
	public void tearDown() throws Exception {
		if (debug) {
			String filename = testName.getMethodName() + ".asta";
			File saveFile = new File(SystemUtils.getJavaIoTmpDir(), filename);
			AstahModelManager.save(saveFile);
			System.out.println(testName.getMethodName() + " result save as "
					+ saveFile.getPath());
		}
		AstahModelManager.close();
	}

	@Test
	public void should_create_domain_in_empty_astah_project() throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("empty.asta"));

		Domain domain = new Domain();
		domain.setLogicalName("test");
		domain.setDataType("VARCHAR");
        IERDomain actual = sut.createAstahModel(domain);

		assertThat(actual.getLogicalName(), is("test"));
	}
	
	@Test
	public void should_create_domain_with_all_properties() throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("empty.asta"));

		Domain domain = new Domain();
		domain.setLogicalName("test");
		domain.setPhysicalName("TEST");
		domain.setDataType("VARCHAR");
		domain.setDefinition("abc");
        IERDomain actual = sut.createAstahModel(domain);

		assertThat(actual.getLogicalName(), is("test"));
		assertThat(actual.getPhysicalName(), is("TEST"));
		assertThat(actual.getDatatypeName(), is("VARCHAR"));
		assertThat(actual.getDefinition(), is("abc"));
	}
	
	@Test
	public void should_create_domain_in_already_exist_ermodel_astah_project() throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("already_exist_ermodel.asta"));

		Domain domain = new Domain();
		domain.setLogicalName("test");
		domain.setDataType("VARCHAR");
        IERDomain actual = sut.createAstahModel(domain);

		assertThat(actual.getLogicalName(), is("test"));
	}
	
	@Test
	public void should_not_create_domain_in_already_exist_ermodel() throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("exist_domain.asta"));

		Domain domain = new Domain();
		domain.setLogicalName("testdomain");
		domain.setDataType("FLOAT");

		try{
            sut.createAstahModel(domain);
			fail();
		}catch(ApplicationException e){
			InvalidEditingException rootCause = (InvalidEditingException)ExceptionUtils.getRootCause(e);
			assertThat(rootCause.getKey(),is(InvalidEditingException.NAME_DOUBLE_ERROR_KEY));
		}
	}
	
	@Test
	public void should_not_create_domain_without_logicalname()
			throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("empty.asta"));
		
		Domain domain = new Domain();
		domain.setDataType("FLOAT");
		try{
            sut.createAstahModel(domain);
			fail();
		}catch(ApplicationException e){
			InvalidEditingException rootCause = (InvalidEditingException)ExceptionUtils.getRootCause(e);
			assertThat(rootCause.getKey(),is(InvalidEditingException.PARAMETER_ERROR_KEY));
		}
	}
	
	@Test
	public void should_create_domain_without_not_exist_datatype()
			throws Exception {
		AstahModelManager.open(getWorkspaceFilePath("empty.asta"));
		
		Domain domain = new Domain();
		domain.setLogicalName("test");
		domain.setPhysicalName("TEST");
		domain.setDataType("NEWTYPE");
		domain.setDefinition("abc");
		
        IERDomain actual = sut.createAstahModel(domain);

		assertThat(actual.getLogicalName(), is("test"));
		assertThat(actual.getPhysicalName(), is("TEST"));
		assertThat(actual.getDatatypeName(), is("NEWTYPE"));
		assertThat(actual.getDefinition(), is("abc"));
	}

    @Test
    public void createAstahModel_parent_difference_in_same_name_domain_creatable() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("addParentDomain.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("CHAR");
        domain.setParentDomain(null);

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getDatatypeName(), is("CHAR"));
        assertThat(actual.getContainer(), is(nullValue()));

        domain = new Domain();
        domain.setLogicalName("Domein1");
        domain.setDataType("CHAR");
        domain.setParentDomain("Domein0");

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein1"));
        assertThat(actual.getDatatypeName(), is("CHAR"));
        IERDomain parentDomain = (IERDomain) actual.getContainer();
        assertThat(parentDomain.getLogicalName(), is("Domein0"));

        domain = new Domain();
        domain.setLogicalName("Domein2");
        domain.setDataType("CHAR");
        domain.setParentDomain("Domein0::Domein1");

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein2"));
        assertThat(actual.getDatatypeName(), is("CHAR"));
        parentDomain = (IERDomain) actual.getContainer();
        assertThat(parentDomain.getLogicalName(), is("Domein1"));
        assertThat(((IERDomain) parentDomain.getContainer()).getLogicalName(), is("Domein0"));
    }

    @Test
    public void createAstahModel_can_import_alias1() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("addAlias1.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("CHAR");
        domain.setAlias1("Alias1");

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getAlias1(), is("Alias1"));
    }
    
    @Test
    public void createAstahModel_can_import_alias2() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("addAlias2.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("CHAR");
        domain.setAlias2("Alias2");

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getAlias2(), is("Alias2"));
    }
    
    @Test
    public void createAstahModel_can_import_length_and_precision() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("addLengthAndPrecision.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("CHAR");
        domain.setLengthAndPrecision("10");

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getLengthPrecision(), is("10"));
    }

    @Test
    public void createAstahModel_can_import_not_null() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("addNotNull.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("CHAR");
        domain.setNotNull("N");

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.isNotNull(), is(true));

        domain = new Domain();
        domain.setLogicalName("Domein1");
        domain.setDataType("CHAR");
        domain.setNotNull("○");

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein1"));
        assertThat(actual.isNotNull(), is(true));

        domain = new Domain();
        domain.setLogicalName("Domein2");
        domain.setDataType("CHAR");
        domain.setNotNull(null);

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein2"));
        assertThat(actual.isNotNull(), is(false));
    }

    @Test
    public void createAstahModel_inputOfLengthPrecisionIsEssentialType_lengthHasNotBeenEntered_setDefaultLengthPrecision()
            throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("setDefaultLengthPrecision.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("TYPE THAT REQUIRES A LENGTH");
        domain.setLengthAndPrecision(null);

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getDatatypeName(), is("TYPE THAT REQUIRES A LENGTH"));
        assertThat(actual.getLengthPrecision(), is("DefaultLength"));

        domain = new Domain();
        domain.setLogicalName("Domein1");
        domain.setDataType("TYPE THAT REQUIRES A PRECISION");
        domain.setLengthAndPrecision(null);

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein1"));
        assertThat(actual.getDatatypeName(), is("TYPE THAT REQUIRES A PRECISION"));
        assertThat(actual.getLengthPrecision(), is("DefaultPrecision"));

        domain = new Domain();
        domain.setLogicalName("Domein2");
        domain.setDataType("TYPE THAT REQUIRES LENGTH AND PRECISION");
        domain.setLengthAndPrecision(null);

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein2"));
        assertThat(actual.getDatatypeName(), is("TYPE THAT REQUIRES LENGTH AND PRECISION"));
        assertThat(actual.getLengthPrecision(), is("DefaultLength,DefaultPrecision"));
    }

    @Test
    public void createAstahModel_addNewDataType() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("add_data_type.asta"));

        Domain domain = new Domain();
        domain.setLogicalName("Domein0");
        domain.setDataType("AAA");
        domain.setLengthAndPrecision(null);

        IERDomain actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein0"));
        assertThat(actual.getDatatypeName(), is("AAA"));
        assertThat(actual.getLengthPrecision(), is(""));

        domain = new Domain();
        domain.setLogicalName("Domein1");
        domain.setDataType("BBB");
        domain.setLengthAndPrecision("10");

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein1"));
        assertThat(actual.getDatatypeName(), is("BBB"));
        assertThat(actual.getLengthPrecision(), is("10"));

        domain = new Domain();
        domain.setLogicalName("Domein2");
        domain.setDataType("CCC");
        domain.setLengthAndPrecision("10,10");

        actual = sut.createAstahModel(domain);

        assertThat(actual.getLogicalName(), is("Domein2"));
        assertThat(actual.getDatatypeName(), is("CCC"));
        assertThat(actual.getLengthPrecision(), is("10,10"));

    }

    @Test
    public void overwriteLogicalName_changeName() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain emptyDomain = domainFinder.find("emptyDomain", "::");
        assertThat(emptyDomain, is(notNullValue()));

        Domain domain = new Domain();
        domain.setLogicalName("newLogicalName");

        try {
            TransactionManager.beginTransaction();
            sut.overwriteLogicalName(emptyDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(emptyDomain.getLogicalName(), is("newLogicalName"));

    }

    @Test
    public void overwritePhysicalName_addName() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain emptyDomain = domainFinder.find("emptyDomain", "::");
        assertThat(emptyDomain, is(notNullValue()));
        assertThat(emptyDomain.getPhysicalName(), is(""));

        Domain domain = new Domain();
        domain.setPhysicalName("newPhysicalName");

        try {
            TransactionManager.beginTransaction();
            sut.overwritePhysicalName(emptyDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(emptyDomain.getPhysicalName(), is("newPhysicalName"));

    }

    @Test
    public void overwritePhysicalName_changeName() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain fullDomain = domainFinder.find("fullDomain", "::");
        assertThat(fullDomain, is(notNullValue()));
        assertThat(fullDomain.getPhysicalName(), is("FULL_DOMAIN"));

        Domain domain = new Domain();
        domain.setPhysicalName("newPhysicalName");

        try {
            TransactionManager.beginTransaction();
            sut.overwritePhysicalName(fullDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(fullDomain.getPhysicalName(), is("newPhysicalName"));

    }

    @Test
    public void overwritePhysicalName_removeName() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain fullDomain = domainFinder.find("fullDomain", "::");
        assertThat(fullDomain, is(notNullValue()));
        assertThat(fullDomain.getPhysicalName(), is("FULL_DOMAIN"));

        Domain domain = new Domain();
        domain.setPhysicalName(null);

        try {
            TransactionManager.beginTransaction();
            sut.overwritePhysicalName(fullDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(fullDomain.getPhysicalName(), is(""));

    }

    @Test
    public void overwriteDatatype_changeExistingDataType() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain emptyDomain = domainFinder.find("emptyDomain", "::");
        assertThat(emptyDomain, is(notNullValue()));

        Domain domain = new Domain();
        domain.setDataType("BIT");

        try {
            TransactionManager.beginTransaction();
            sut.overwriteDatatype(emptyDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(emptyDomain.getDatatypeName(), is("BIT"));

    }

    @Test
    public void overwriteDatatype_changeNewDataType() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain emptyDomain = domainFinder.find("emptyDomain", "::");
        assertThat(emptyDomain, is(notNullValue()));

        Domain domain = new Domain();
        domain.setDataType("AAA");

        try {
            TransactionManager.beginTransaction();
            sut.overwriteDatatype(emptyDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat(emptyDomain.getDatatypeName(), is("AAA"));

    }

    @Test
    public void overwriteParentDomain_addParent() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain emptyDomain = domainFinder.find("emptyDomain", "::");
        assertThat(emptyDomain, is(notNullValue()));
        IERDomain fullDomain = domainFinder.find("fullDomain", "::");
        assertThat(fullDomain, is(notNullValue()));

        Domain domain = new Domain();
        domain.setLogicalName(emptyDomain.getLogicalName());
        domain.setParentDomain(fullDomain.getLogicalName());

        try {
            TransactionManager.beginTransaction();
            sut.overwriteParentDomain(emptyDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat((IERDomain) emptyDomain.getContainer(), is(fullDomain));

    }

    @Test
    public void overwriteParentDomain_removeParent() throws Exception {
        AstahModelManager.open(getWorkspaceFilePath("overwriteAstahModel.asta"));

        IERDomain hasParentDomain = domainFinder.find("fullDomain::hasParentDomain", "::");
        assertThat(hasParentDomain, is(notNullValue()));

        Domain domain = new Domain();
        domain.setLogicalName(hasParentDomain.getLogicalName());
        domain.setParentDomain(null);

        try {
            TransactionManager.beginTransaction();
            sut.overwriteParentDomain(hasParentDomain, domain);
            TransactionManager.endTransaction();
        } catch (Exception e) {
            TransactionManager.abortTransaction();
            throw e;
        }

        assertThat((IERDomain) hasParentDomain.getContainer(), is(nullValue()));

    }

	private URL getWorkspaceFilePath(String filename) {
		return this.getClass().getResource(filename);
	}
}
