package com.xuejungao.core;

import com.xuejungao.FileUtils.FileUtils;
import com.xuejungao.entity.Service;
import com.xuejungao.entity.TestCase;
import com.xuejungao.saxxml.SaxHandler;
import com.xuejungao.saxxml.SaxHostHandler;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 代表一个测试集合 TestSuit
public class Gunit extends ParentRunner<TestSuitRunner> {

    // 定义一个List 类型,用来装测试用例
    private List<TestSuitRunner> testSuitRunnerList = new ArrayList<TestSuitRunner>();

    // 声明测试用例列表
    private List<TestCase>  testCaseList = new ArrayList<>();
    // 声明 HOst列表
    private List<Service> serviceList = new ArrayList<>();
    // 声明map集合
    private Map<String,Service> serviceMap = new HashMap<>();
    // 获取应用上下文，加载xml
    private ApplicationContext context;


    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     * 创建构造函数
     * @param testClass
     */
    public Gunit(Class<?> testClass) throws InitializationError {
        super(testClass);

        // 调用获取文件的地方法
        getAllFileXml();

        // 调用解析 Host
        getServiceFile();

        // 获取 id 和tag的值
        InterceptorClasses interceptorClasses = testClass.getAnnotation(InterceptorClasses.class);

        // 获取注解的内容
        String[] ids = interceptorClasses.ids();
        // 获取tag值
        String[] tags = interceptorClasses.tags();

        // 首选如果id不为空执行id,如果id为空tag不为空，执行tag,如果id和tag都为空，执行全量
        if(ids[0] != null && ids[0].length() > 0){
            // 执行id
            for (int i=0; i< testCaseList.size(); i++){
                for (String id : ids){
                    if(id.equals(testCaseList.get(i).getId())){
                        // 实例化 对象 TestSuitRunner
                        TestSuitRunner testSuitRunner = new TestSuitRunner(testClass,testCaseList.get(i),serviceMap);
                        // 加入到列表里面
                        testSuitRunnerList.add(testSuitRunner);
                    }
                }
            }

        }

        if((ids[0] == null || ids[0].length() == 0) && (tags[0] != null && tags[0].length() > 0)){
            // 执行tag
            for (int i=0; i< testCaseList.size(); i++){
                for (String tag : tags){
                    if(tag.equals(testCaseList.get(i).getTag())){
                        // 实例化 对象 TestSuitRunner
                        TestSuitRunner testSuitRunner = new TestSuitRunner(testClass,testCaseList.get(i),serviceMap);
                        // 加入到列表里面
                        testSuitRunnerList.add(testSuitRunner);
                    }
                }
            }
        }

        if((ids[0] == null || ids[0].length() == 0 ) && (tags[0] == null || tags[0].length() == 0)){
            // 执行全量
            // 使用for循环 执行测试用例
            for (int i=0; i< testCaseList.size(); i++){

                // 实例化 对象 TestSuitRunner
                TestSuitRunner testSuitRunner = new TestSuitRunner(testClass,testCaseList.get(i),serviceMap);
                // 加入到列表里面
                testSuitRunnerList.add(testSuitRunner);

            }
        }
        System.out.println("当前需要运行的测试用例:"+ids.length +"====="+tags.length);

//        for (String id : ids){
//
//            System.out.println("当前需要运行的测试用例:"+id);
//        }
//
//        for (String tag : tags){
//
//            System.out.println("当前需要运行的测试用例是:tag"+tag);
//        }


        // 测试用例运行的总体逻辑是如果有 id 就运行 id,如果没有id就运行Tag,如果没有tag,就运行全部测试用例

        // 获取放置数据库配置文件路径path
        String path = getClass().getClassLoader().getResource("config").getPath();
//        // 加载数据库配置文件
//        // /Users/gaoxuejun/Desktop/Desktop/java_tool_develop/GunitTool/src/test/resource/config/spring/spring-mybatis.xml
//        context = new ClassPathXmlApplicationContext(path+"/spring-mybatis.xml");
//        // 获取bean对象
//        LoginMapperDao loginMapperDao = (LoginMapperDao) context.getBean("LoginMapperDao");

//
//        if(null == loginMapperDao){
//
//            System.out.println("对象不为空");
//        }


//        try {
//            //使用MyBatis提供的Resources类加载mybatis的配置文件
//            Reader reader = Resources.getResourceAsReader(path+"/spring/spring-mybatis.xml");
//            //构建sqlSession的工厂
//            SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(reader);
//
//            SqlSession session= sessionFactory.openSession();
//
//            LoginMapperDao mapper=session.getMapper(LoginMapperDao.class);
//
//            Map<String,String> map = mapper.findLoginByName(" select * from qiezzi_login where username=='ASasd123' ");
//
//            System.out.println("当前的长度是："+map.size());
////            User user= mapper.GetUserByID(1);
////            System.out.println(user.toString());
//
//            session.commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }



    }


    // 执行的测试用例条数
    @Override
    protected List getChildren() {

        return testSuitRunnerList;
    }

    @Override
    protected Description describeChild(TestSuitRunner child) {

        // 测试用例的描述
        return child.getDescription();
    }

    @Override
    protected void runChild(TestSuitRunner child, RunNotifier notifier) {


        // 执行测试用例
        child.run(notifier);
    }


    // 获取 service 路径
    public void  getServiceFile(){

        // 获取当前 cases文件路径
        String path = getClass().getClassLoader().getResource("service").getPath();
        // 对下面文件进行遍历,获取所有的,xml文件
        List<File> fileList = FileUtils.getFileList(path);

        // 使用for循环遍历
        for (File file :  fileList){

            saxServiceXml(file.toString());
        }

    }

    // 获取路径的方法
    public void getAllFileXml(){

        System.out.println("当前的对象"+getClass().getClassLoader());
        System.out.println("当前的对象"+getClass().getClassLoader().getResource("case"));

        // 获取当前 cases文件路径
        String path = getClass().getClassLoader().getResource("case").getPath();
        // 对下面文件进行遍历,获取所有的,xml文件
        List<File> fileList = FileUtils.getFileList(path);

        // 使用 for 循环进行遍历
        for (File file : fileList){

            saxXml(file.toString());
        }
    }


    // 封装解析 service xml 的方法
    public void saxServiceXml(String path){

        // 解析数据
        //创建解析工厂
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // 获取  SAXParser
        SAXParser parser = null;

        try {

            parser = factory.newSAXParser();

            //实例化自定义
            SaxHostHandler handler = new SaxHostHandler();

            // 获取路径

            // 进行解析
            parser.parse(path, handler);
            // 每个xml里面所有的测试用例加入到 List 里面
            serviceList.addAll(handler.getServiceList());

            // 将 List列表转为map集合,key 就是 id
            for (Service service : serviceList){

                serviceMap.put(service.getId(),service);
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 封装xml解析方法
    public void saxXml(String path){
        // 解析数据
        //创建解析工厂
        SAXParserFactory factory = SAXParserFactory.newInstance();
        // 获取  SAXParser
        SAXParser parser = null;

        try {

            parser = factory.newSAXParser();

            //实例化自定义
            SaxHandler handler = new SaxHandler();

            // 获取路径

            // 进行解析
            parser.parse(path, handler);
            // 每个xml里面所有的测试用例加入到 List 里面
            testCaseList.addAll(handler.getTestCaseList());

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
