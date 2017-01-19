# RealmTest
## 介绍
Realm 是一个 [MVCC](https://en.wikipedia.org/wiki/Multiversion_concurrency_control) （多版本并发控制）数据库，由Y Combinator公司在2014年7月发布一款支持运行在手机、平板和可穿戴设备上的嵌入式数据库，目标是取代SQLite。
Realm 本质上是一个嵌入式数据库，他并不是基于SQLite所构建的。它拥有自己的数据库存储引擎，可以高效且快速地完成数据库的构建操作。和SQLite不同，它允许你在持久层直接和数据对象工作。在它之上是一个函数式风格的查询api，众多的努力让它比传统的SQLite 操作更快 。
[详细介绍](https://realm.io/cn/news/jp-simard-realm-core-database-engine/)（如果进不去，看[这个](https://www.zybuluo.com/pockry/note/453560)也行）

## 优势
如果在在使用它时候，连它的优势在哪都不知道的话就有点说不过去了。
   - **易用**
Ream 不是在SQLite基础上的ORM，它有自己的数据查询引擎。并且十分容易使用。
   - **快速**
由于它是完全重新开始开发的数据库实现，所以它比任何的ORM速度都快很多，甚至比SLite速度都要快。
   - **跨平台**
Realm 支持 iOS & OS X (Objective‑C & Swift) & Android。我们可以在这些平台上共享Realm数据库文件，并且上层逻辑可以不用任何改动的情况下实现移植。
   - **高级**
Ream支持加密，格式化查询，易于移植，支持JSON，流式api，数据变更通知等高级特性
   - **可视化**
Realm 还提供了一个轻量级的数据库查看工具，在Mac Appstore 可以下载“Realm Browser”这个工具，开发者可以查看数据库当中的内容，执行简单的插入和删除数据的操作。（windows上还不清楚）

## 条件
 - 目前不支持`Android`以外的`Java`
 - Android Studio >= 1.5.1
 - 较新的Android SDK版本
 - JDK version >=7.
 - 支持API 9(Android 2.3)以及之后的版本

## 使用
不介绍了，看代码了。
- **加入依赖**
在`project`的`build`中加入依赖
```java
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "io.realm:realm-gradle-plugin:2.2.1"
    }
}
```
![project/build](http://upload-images.jianshu.io/upload_images/1638147-efd0042285b22de4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
在`module`中加入
```java
apply plugin: 'realm-android'
```
![module/build](http://upload-images.jianshu.io/upload_images/1638147-75617c94f75a302f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
> Realm从v1.0.0后，不支持Ecilpse，我们推荐使用Android Studio

### **创建model**
创建一个`User`类，需要继承`RealmObject`。支持public, protected和 private的类以及方法
```java
public class User extends RealmObject {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
```
除了直接继承于`RealmObject`来声明 Realm 数据模型之外，还可以通过实现 `RealmModel`接口并添加 `@RealmClass`修饰符来声明。
```java
@RealmClass
public class User implements RealmModel {
    ...
}
```

 - 支持的属性
`boolean`, `byte`, `short`,` int`,` long`,` float`, `double`,` String`, `Date` 和，`byte[]`, `RealmObject`, `RealmList<? extends RealmObject>`
还支持`Boolean`, `Byte`, `Short`, `Integer`, `Long`, `Float` 和 `Double`
**Tip**：整数类型 `short`、`int` 和 `long` 都被映射到 Realm 内的相同类型（实际上为 long ）
 - `@PrimaryKey`——表示该字段是主键
使用过数据库的同学应该看出来了，`PrimaryKey`就是主键。使用`@PrimaryKey`来标注，字段类型必须是字符串（`String`）或整数（`byte`，`short`，`int`或`long`）以及它们的包装类型（`Byte`,` Short`, `Integer`, 或 `Long`）。不可以存在多个主键，使用字符串字段作为主键意味着字段被索引（注释`@PrimaryKey`隐式地设置注释`@Index`）。
```java
@PrimaryKey
private String id;
```
 - `@Required`——表示该字段非空
在某些情况下，有一些属性是不能为`null`的。使用`@Required`可用于用于强行要求其属性不能为空，只能用于`Boolean`, `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `String`, `byte[]` 和 `Date`。在其它类型属性上使用 `@Required`修饰会导致编译失败。
**Tip**：基本数据类型不需要使用注解 `@Required`，因为他们本身就不可为空。
```java
@Required
private String name;
```
 - `@Ignore`——表示忽略该字段
被添加`@Ignore`标签后，存储数据时会忽略该字段。
```java
@Ignore
private String name;
```
 - `@Index`——添加搜索索引
为字段添加搜索索引，这样会使得插入的速度变慢，数据量也变得更大。不过在查询速度将变得更快，建议只在优化读取性能的特定情况时添加索引。支持索引：`String`，`byte`，`short`，`int`，`long`，`boolean`和`Date`字段。
```


 > **注意**：如果你创建Model并运行过，然后修改了Model。那么就需要升级数据库，否则会抛异常。升级方式后面会提到

### **初始化**
使用默认配置
```java
        Realm.init(this);
        Realm mRealm = Realm.getDefaultInstance();
```
这时候会创建一个叫做 `default.realm`的Realm文件，一般来说，这个文件位于/data/data/包名/files/。通过`realm.getPath()`来获得该Realm的绝对路径。
> 注意：模拟器上运行时，`Realm.getDefaultInstance()`抛出异常，真机上没问题（不止何故）

当然，我们还可以使用`RealmConfiguration`来配置Realm
```java
RealmConfiguration config = new RealmConfiguration.Builder() 
            .name("myrealm.realm") //文件名
            .schemaVersion(0) //版本号
            .build();
Realm realm = Realm.getInstance(config);
```
创建非持久化的Realm，也就是保持在内存中，应用关闭后就清除了。
```java
RealmConfiguration myConfig = new RealmConfiguration.Builder(context) 
            .name("myrealm.realm")//保存在内存中
            .inMemory() .build();
```
`RealmConfiguration`支持的方法：
 - `Builder.name` : 指定数据库的名称。如不指定默认名为default。
 - `Builder.schemaVersion` : 指定数据库的版本号。
 - `Builder.encryptionKey` : 指定数据库的密钥。
 - `Builder.migration` : 指定迁移操作的迁移类。
 - `Builder.deleteRealmIfMigrationNeeded` : 声明版本冲突时自动删除原数据库。
 - `Builder.inMemory` : 声明数据库只在内存中持久化。
 - `build` : 完成配置构建。

### **关闭Realm**
记得使用完后，在`onDestroy`中关闭Realm
```java
@Override 
protected void onDestroy() { 
    super.onDestroy();
    // Close the Realm instance. 
    realm.close(); 
}
```

### **增**
写入操作需要在事务中进行，可以使用`executeTransaction`方法来开启事务。
 - 使用`executeTransaction`方法插入数据
```java
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Gavin");
                user.setAge(23);
            }
        });
```
在`execute`方法中执行插入操作
> **注意**：如果在UI线程中插入过多的数据，可能会导致主线程拥塞。

- 使用`copyToRealmOrUpdate`或`copyToRealm`方法插入数据
当Model中存在**主键**的时候，推荐使用`copyToRealmOrUpdate`方法插入数据。如果对象存在，就更新该对象；反之，它会创建一个新的对象。若该Model没有主键，使用`copyToRealm`方法，否则将抛出异常。
```java
        final User user = new User();
        user.setName("Jack");
        user.setId("2");
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(user);
            }
        });
```


 - 如果你用的是这样的`modle`
```java
public class User2 extends RealmObject {
      public String name;
      public int age;
}
```
就这样写
```java
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User2 user = realm.createObject(User2.class);
                user.name = "Micheal";
                user.age = 30;
            }
        });
```
 - 如果User还有其他属性是，比如养了只`Dog`：
```java
public class Dog extends RealmObject {
      private String name;
      private int age;
      //getters and setters
}
```
对应的User属性中加入`Dog`类
```java
public class User extends RealmObject {
      private String name;
      private int age;
      private Dog dog;
      //getters and setters
}
```
 如果你养了不止一只，你养了二哈、阿拉撕家...那你就要用到`RealmList`了。在`User`中加入该属性
```java
private RealmList<Dog> dogs;
```
插入数据
```java
    @Override
    public void execute(Realm realm) {
        User user = realm.createObject(User.class);
        user.setName("Gain");
        user.setAge(23);

        Dog dog1 = realm.createObject(Dog.class);
        dog1.setAge(1);
        dog1.setName("二哈");
        user.getDogs().add(dog1);

        Dog dog2 = realm.createObject(Dog.class);
        dog2.setAge(2);
        dog2.setName("阿拉撕家");
        user.getDogs().add(dog2);
      }
});
```
 - 上面都是用可`executeTransaction`方法插入数据，还有另一种方法可以用于插入数据——`beginTransaction`和`commitTransaction`
```java
mRealm.beginTransaction();//开启事务
User user = mRealm.createObject(User.class);
user.setName("Gavin");
user.setId("3");
mRealm.commitTransaction();//提交事务
```
在插入前，先调用`beginTransaction()`，完成后调用`commitTransaction()`即可。
> **注意**：在UI和后台线程同时开启创建write的事务，可能会导致ANR错误。为了避免该问题，可以使用`executeTransactionAsync`来实现。

 - 使用`executeTransactionAsync`
该方法会开启一个子线程来执行事务，并且在执行完成后进行结果通知。
```java
RealmAsyncTask transaction = mRealm.executeTransactionAsync(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        User user = realm.createObject(User.class);
        user.setName("Eric");
        user.setId("4");
      }
});
```
还可以加入监听
```java
RealmAsyncTask transaction =  mRealm.executeTransactionAsync(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        User user = realm.createObject(User.class);
        user.setName("Eric");
        user.setId("4");
      }
}, new Realm.Transaction.OnSuccess() {
    @Override
    public void onSuccess() {
        //成功回调
      }
}, new Realm.Transaction.OnError() {
    @Override
    public void onError(Throwable error) {
        //失败回调
      }
});
```
**注意：如果当`Acitivity`或`Fragment`被销毁时，在`OnSuccess`或`OnError `中执行UI操作，将导致程序奔溃 。用`RealmAsyncTask .cancel();`可以取消事务**
在`onStop`中调用，避免crash
```java
public void onStop () {
    if (transaction != null && !transaction.isCancelled()) {
        transaction.cancel();
      }
}
```

- JSON
Realm还是个很nice的功能就是将Json字符串转化为对象，厉害了我的Realm
（直接借用官方的例子）
```java
// 一个city model
public class City extends RealmObject {
    private String city;
    private int id;
    // getters and setters left out ...
}
// 使用Json字符串插入数据
realm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        realm.createObjectFromJson(City.class, "{ city: \"Copenhagen\", id: 1 }");
    }
});
// 使用InputStream插入数据
realm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        try {
            InputStream is = new FileInputStream(new File("path_to_file"));
            realm.createAllFromJson(City.class, is);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
});
```
Realm 解析 JSON 时遵循如下规则： 
 * 使用包含空值（null）的 JSON 创建对象：
  * 对于非必须（可为空值的属性），设置其值为 null；
 * 对于必须（不可为空值的属性），抛出异常； 
 * 使用包含空值（null）的 JSON 更新对象：
 * 对于非必须（可为空值的属性），设置其值为 null；
 * 对于必须（不可为空值的属性），抛出异常；
 * 使用不包含对应属性的 JSON： * 该属性保持不变

### **查**
查找操作就比插入方便多了，并不需在事务中操作，直接查询即可。
 - `findAll` ——查询
例：查询所有的`User`
```java
RealmResults<User> userList = mRealm.where(User.class).findAll();
```
这里使用`RealmResults`来接受查询到的结果，突然出现的`RealmResults`可能会让人懵逼。看看他的源码，发现`RealmResults`继承了`AbstractList`，而`AbstractList`又实现了`List`接口。好吧，原来实现了我们熟悉的`List`接口。
```java
public final class RealmResults<E extends RealmModel> extends AbstractList<E>
```
```java
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> 
```
![RealmResults源码](http://upload-images.jianshu.io/upload_images/1638147-df2c5fd79a1d0445.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
> **注意**：`RealmResults`虽然实现了`List`接口，不过有很多方法是不能用的。比如`add`、`addAll`、`remove`、`clear`等，调用后会直接抛异常。不过也不用当心误用这些方法，因为它们都被标记为`@Deprecated`了。

- `findAllAsync`——异步查询
当数据量较大，可能会引起ANR的时候，就可以使用`findAllAsync`
```java
        RealmResults<User> userList = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .findAllAsync();
```
值得注意的是，这里并不会马上查到数据，是有一定延时的。也就是说，你马上使用`userList`的时候，里面是没有数据的。可以注册`RealmChangeListener`监听器，或者使用`isLoaded()`方法，判断是否查询完成
```java
if (result.isLoaded()) {
     // 完成查询
}
```


 - `findFirst` ——查询第一条数据
例：查询`User`表中的第一条数据
```java
User user2 = mRealm.where(User.class).findFirst();
```
 - `equalTo` ——根据条件查询
例：得到`name`为`Gavin`的用户列表。
```java
RealmResults<User> userList = mRealm.where(User.class)
            .equalTo("name", "Gavin").findAll();
```
 如果`User`中还有`Dog`属性，希望根据`Dog`的条件来获取用户：
例：查询`dogs.name`为二哈的`User`
```java
RealmResults<User> userList = mRealm.where(User.class)
             .equalTo("dogs.name", "二哈").findAll();
```
得到有养有`dogs.name`为"二哈"的用户列表（这里的`dogs`是`User`表中的属性名）
 - `equalTo` ——多条件查询
当然，我们还经常要用到多条件的查询的功能。
例：找到用户名为“Gavin”，且`dogs.name`为“二哈”的`User`
```java
RealmResults<User> userList = mRealm.where(User.class)
            .equalTo("name", "Gavin").findAll();
RealmResults<User> userList = user5.where()
            .equalTo("dogs.name", "二哈").findAll();
```
上面先找到`name`为“Gavin”的`User`列表，然后再得到的结果中查询`dogs.name`为“二哈”
觉得这样写太麻烦？我也是这样想的，所以还可以这样写
```java
RealmResults<User> userList = mRealm.where(User.class)
            .equalTo("name", "Gavin")
            .equalTo("dogs.name", "二哈")
            .findAll();
```
是不是很清爽~~~
 - 更多查询条件
上面就展示了`equalTo`的用法。当然，查询还有更多的用法，我就不一一示例了。已知的方法如下：
`sum()`：对指定字段求和。
`average()`：对指定字段求平均值。
`min() `: 对指定字段求最小值。
`max()` : 对指定字段求最大值。count : 求结果集的记录数量。
`findAll() `: 返回结果集所有字段，返回值为RealmResults队列
`findAllSorted()` : 排序返回结果集所有字段，返回值为RealmResults队列
`between()`, `greaterThan()`,` lessThan()`, `greaterThanOrEqualTo()` & `lessThanOrEqualTo()`
`equalTo()` & `notEqualTo()`
`contains()`, `beginsWith()` & `endsWith()`
`isNull()` & `isNotNull()`
`isEmpty() `& `isNotEmpty()`

- `RealmQuery`以及`or`的使用
在使用`where()`方法时，能得到一个`RealmQuery`对象，使用方法如下：
例：查询`name`为“Gavin”和“Eric”的用户
```java
RealmQuery<User> query = mRealm.where(User.class);
query.equalTo("name", "Gavin");
query.or().equalTo("name", "Eric");
RealmResults<User> userList = query.findAll();
```
这种情况下就要用到or()方法
这么一大串，你又觉得麻烦了？没事，继续简化。
```java
RealmResults<User> userList = mRealm.where(User.class)
            .equalTo("name", "Gavin")
            .or().equalTo("name", "Eric")
            .findAll();
```
**Tip**：查询的时候你不用当心得到的`RealmResults`为`null`。如果查询的结果为空，那么`RealmResults`的`size`为0

- 排序
查询结束后，还可以进行排序，就像这样：
```java
RealmResults<User> userList = mRealm.where(User.class) .findAll();
result = result.sort("age"); //根据age，正序排列
result = result.sort("age", Sort.DESCENDING);//逆序排列
```

- 聚合
`RealmResult`自带一些聚合方法：
```java
RealmResults<User> results = realm.where(User.class).findAll();
long   sum     = results.sum("age").longValue();
long   min     = results.min("age").longValue();
long   max     = results.max("age").longValue();
double average = results.average("age");
long   matches = results.size();
```

### **改**
```java
mRealm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        //先查找后得到User对象
        User user = mRealm.where(User.class).findFirst();
        user.setAge(26);
    }
});
```
修改也是需要在事务中操作。
使用查询语句得到数据，然后将内容改了即可。

### **删**
- 使用`deleteFromRealm()`
```java
//先查找到数据
final RealmResults<User> userList = mRealm.where(User.class).findAll();
mRealm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        userList.get(0).deleteFromRealm();
    }
});
```
- 使用`deleteFromRealm(int index)`
```java
mRealm.executeTransaction(new Realm.Transaction() {
    @Override
    public void execute(Realm realm) {
        userList.deleteFromRealm(0);
    }
});
```
- 更多方法：
```java
userList.deleteFirstFromRealm(); //删除user表的第一条数据
userList.deleteLastFromRealm();//删除user表的最后一条数据
results.deleteAllFromRealm();//删除user表的全部数据
```

### **版本升级**
当数据结构发生变化是，需要升级数据库。对于Realm来说，数据库升级就是迁移操作，把原来的数据库迁移到新结构的数据库。（体验：略麻烦）
 - 例1：`User`类发生变化，移除`age`，新增个`@Required`的`id`字段。
 `User`版本：version 0 
```java
String name;
int    age;
```
 `User`版本：version 1 
```java
@Required
String    id;
String name;
```
 创建迁移类`CustomMigration`，需要实现`RealmMigration`接口。执行版本升级时的处理：
```java
    /**
     * 升级数据库
     */
    class CustomMigration implements RealmMigration {
        @Override
        public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 0 && newVersion == 1) {
                RealmObjectSchema personSchema = schema.get("User");
                //新增@Required的id
                personSchema
                        .addField("id", String.class, FieldAttribute.REQUIRED)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicReal
mObject obj) {
                                obj.set("id", "1");//为id设置值
                            }
                        })
                        .removeField("age");//移除age属性
                oldVersion++;
            }
        }
    }
```
 使用`Builder.migration`升级数据库，将版本号改为1（原版本号：0）。当Realm发现新旧版本号不一致时，会自动使用该迁移类完成迁移操作。
```java
RealmConfiguration config = new RealmConfiguration.Builder() 
            .name("myrealm.realm") //文件名
            .schemaVersion(1) 
            .migration(new CustomMigration())//升级数据库
            .build();
```
 - 例2：加入`Dog`类，`User`中加入`Dog`集合。
 `User`版本：version 1 
```java
@Required
String    id;
String name;
``` 
`User`版本：version 2
```java
@Required
private String id;
private String name;
private RealmList<Dog> dogs;
```
`Dog`类
```java
public class Dog extends RealmObject {
        private String name;
        private int age;
}
```
在迁移类`CustomMigration`中，继续添加处理方法。
```java
            if (oldVersion == 1 && newVersion == 2) {
                //创建Dog表
                RealmObjectSchema dogSchema = schema.create("Dog");
                dogSchema.addField("name", String.class);
                dogSchema.addField("age", int.class);

                //User中添加dogs属性
                schema.get("User")
                        .addRealmListField("dogs", dogSchema)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                //为已存在的数据设置dogs数据
                                DynamicRealmObject dog = realm.createObject("Dog");
                                dog.set("name", "二哈");
                                dog.set("age", 2);
                                obj.getList("dogs").add(dog);
                            }
                        });
                oldVersion++;
            }
```
 - 更多用法——`RealmObjectSchema`
例：取消id必填：
```java
personSchema.setNullable("id", true):
```
例：移除id字段
```java
personSchema.removeField("id");
```
例：重命名
```java
personSchema..renameField("id", "userId");
```
 - 更多用法——`DynamicRealmObject`
例：获取id
```java
String id = obj.getString("id");
```
例：为字段设置值
```java
obj.setString("name", "Gavin");
obj.setInt("id", 1);
obj.setLong("id", 1);
```
> **疑问**：我在debug时发现这样的数据，如图：
![userList](http://upload-images.jianshu.io/upload_images/1638147-cf72a8f57a6bc79b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
什么，dogs、id、name的值怎么都是null？这~~
开始我还以为升级时数据丢失，不过在图中userList第0条数据的右边看到了我的数据，于是我展开了里面的内容，如图：
![展开后的userListg](http://upload-images.jianshu.io/upload_images/1638147-ff78174ea77a57d0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
好吧，他并不是我想象中的那种存法，我已经不知道他内部是怎么实现的了。（还望大神赐教）

###加密
（官方原文）
Realm 文件可以通过传递一个512位（64字节）的密钥参数给 `Realm.getInstance().encryptionKey()` 来加密存储在磁盘上。
```java
byte[] key = new byte[64];
new SecureRandom().nextBytes(key);
RealmConfiguration config = new RealmConfiguration.Builder()
  .encryptionKey(key)
  .build();
Realm realm = Realm.getInstance(config);
```
保证了所有永久性存储在磁盘上的数据都是通过标准 AES-256 加密的。每次创建新的 Realm 实例的时候，都需要提供相同的密钥。
参考 [examples/encryptionExample](https://github.com/realm/realm-java/tree/master/examples/encryptionExample)。这个例子演示了如何通过 Android KeyStore 来安全地存储密钥。

###适配器（Adapter）
（官方原文）
Realm提供了一些抽象的工具类来方便地将 `OrderedRealmCollection`
（`RealmResults`和 `RealmList` 都实现了这个借口）展示到UI控件上。
- `RealmBaseAdapter` 可以与` ListView`配合使用。参见[示例](https://github.com/realm/realm-android-adapters/blob/master/example/src/main/java/io/realm/examples/adapters/ui/listview/MyListAdapter.java).
- `RealmRecyclerViewAdapter`可以与 `RecyclerView`配合使用。参见[示例](https://github.com/realm/realm-android-adapters/blob/master/example/src/main/java/io/realm/examples/adapters/ui/recyclerview/MyRecyclerViewAdapter.java).

你需要在 app 的 `build.gradle`中添加额外的依赖以使用这些适配器。
```java
dependencies {
	compile 'io.realm:android-adapters:1.4.0'
}
```

###Intents
（官方原文）
你不可以直接通过 `intent `传递 `RealmObject`，建议你只传递`RealmObject`的标识符。举个常用例子，假如你的对象拥有一个主键，请通过` intent` 的 `bundle` 来传递这个主键的值。
```java
// Assuming we had a person class with a @PrimaryKey on the 'id' field ...
Intent intent = new Intent(getActivity(), ReceivingService.class);
intent.putExtra("person_id", person.getId());
getActivity().startService(intent);
```
在接受方（`Activity`、`Service`、`IntentService`、`BroadcastReceiver `及其它）从 `bundle `中解析出这个主键然后打开` Realm` 查询得到这个 `RealmObject`。
```java
// in onCreate(), onHandleIntent(), etc.
String personId = intent.getStringExtra("person_id");
Realm realm = Realm.getDefaultInstance();
Person person = realm.where(Person.class).equalTo("id", personId).findFirst();// do something with the person ...
realm.close();
```
可以参考 [threading example](https://github.com/realm/realm-java/tree/master/examples/threadExample) 中的 Object Passing部分。该示例展示了在安卓开发中常用的如何传递 id 并且得到对应的 `RealmObject`。

###RxJava
对于这么火的RxJava，Realm又怎么会放过他。
Realm 包含了对 RxJava 的原生支持。如下类可以被暴露为一个 [Observable](https://github.com/ReactiveX/RxJava/wiki/Observable)：[Realm](https://realm.io/cn/docs/java/latest/api/io/realm/Realm.html#asObservable--), [RealmResults](https://realm.io/cn/docs/java/latest/api/io/realm/RealmResults.html#asObservable--), [RealmObject](https://realm.io/cn/docs/java/latest/api/io/realm/RealmObject.html#asObservable--), [DynamicRealm](https://realm.io/cn/docs/java/latest/api/io/realm/DynamicRealm.html#asObservable--) 和 [DynamicRealmObject](https://realm.io/cn/docs/java/latest/api/io/realm/DynamicRealmObject.html#asObservable--)。
（直接用一个官方的例子）
```java
Realm realm = Realm.getDefaultInstance();
GitHubService api = retrofit.create(GitHubService.class);
realm.where(Person.class).isNotNull("username").findAllAsync().asObservable()
    .filter(persons.isLoaded)
    .flatMap(persons -> Observable.from(persons))
    .flatMap(person -> api.user(person.getGithubUserName())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(user -> showUser(user));
```
这里使用`asObservable`轻松转换成RxJava中的`Observable`，使用`.filter(persons.isLoaded)`来判断是否已查询完成。到这里，熟悉RxJava的同学应该已经看明白了~~。可能还有人会一脸懵逼，`->`? 这是什么鬼？哈哈，这叫Lambda，有时间可以去看看。
> 也许还有很多同学不了解RxJava，这里极力推荐[给 Android 开发者的 RxJava 详解](https://gank.io/post/560e15be2dca930e00da1083)。

终于写完了~~~说好易用的，没想到内容居然这么多。

##参考资料
[官方文档](https://realm.io/docs/java/latest/)
[GitHub](https://github.com/realm/realm-java)
[Realm for Android快速入门教程](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/1226/3807.html)
[Android开发笔记（八十五）手机数据库Realm](http://blog.csdn.net/aqi00/article/details/51145001)
 [在Android加入和使用Realm](http://blog.csdn.net/qibin0506/article/details/49905643)
[Android 进阶之第三方库的介绍 Realm [一] 基础用法](http://coderlin.coding.me/2016/06/11/Android-%E8%BF%9B%E9%98%B6%E4%B9%8B%E7%AC%AC%E4%B8%89%E6%96%B9%E5%BA%93%E7%9A%84%E4%BB%8B%E7%BB%8D-Realm/)

> 以上有错误之处，感谢指出
