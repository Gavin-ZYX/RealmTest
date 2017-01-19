package com.example.gavin.realmtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.RealmSchema;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "MainActivity";
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRealm();
        //createUser();
        //retrieveUser();
        //updateUser();

    }

    /**
     * 初始化Realm
     */
    private void initRealm() {
        initView();
        Realm.init(this);
        //mRealm = Realm.getDefaultInstance();
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name("test.realm")
                .schemaVersion(2)
                .migration(new CustomMigration())//升级数据库
                //.deleteRealmIfMigrationNeeded()
                .build();

        mRealm = Realm.getInstance(configuration);
        String str = mRealm.getPath();
        Log.i(TAG, str);
    }

    /**
     * 升级数据库
     */
    class CustomMigration implements RealmMigration {
        @Override
        public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            /************************************************
             // Version 0
             class User
             String name;
             int    age;

             // Version 1
             class User
             @Required int    id;
             String name;
             ************************************************/
            if (oldVersion == 0 && newVersion == 1) {
                RealmObjectSchema personSchema = schema.get("User");
                //新增@Required的id
                personSchema
                        .addField("id", String.class, FieldAttribute.REQUIRED)
                        .transform(new RealmObjectSchema.Function() {
                            @Override
                            public void apply(DynamicRealmObject obj) {
                                obj.set("id", "1");//为id设置值
                            }
                        })
                        .removeField("age");//移除age属性
                oldVersion++;
            }

            /************************************************
             // Version 0
             class User
             @Required int    id;
             String name;

             // Version 1
             class Cat
             String name;
             int age;

             class User
             @Required int    id;
             String name;
             RealmList<Dog> cats;
             ************************************************/
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
                                //为已存在的数据设置Cat类
                                DynamicRealmObject dog = realm.createObject("Dog");
                                dog.set("name", "二哈");
                                dog.set("age", 2);
                                obj.getList("dogs").add(dog);
                            }
                        });
                oldVersion++;
            }
            if(oldVersion == 2 && newVersion == 3) {

            }
        }
    }

    private void initView() {
        findViewById(R.id.create).setOnClickListener(this);
        findViewById(R.id.retrieve).setOnClickListener(this);
        findViewById(R.id.update).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create:
                //createUser();
                createdTest1();
                break;
            case R.id.retrieve:
                //retrieveUser();
                retrieveTest1();
                break;
            case R.id.update:
                updateUser();
                break;
            case R.id.delete:
                deleteUser();
                break;
        }
    }

    private boolean isTest = true;
    private int i = 0;

    private void createdTest1() {
        /*RealmAsyncTask transaction = mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Michale");
                user.setId(i + "");

                Dog dog1 = realm.createObject(Dog.class);
                dog1.setAge(1);
                dog1.setName("二哈");
                user.getDogs().add(dog1);

                Dog dog2 = realm.createObject(Dog.class);
                dog2.setAge(2);
                dog2.setName("阿拉撕家");
                user.getDogs().add(dog2);
                i++;
            }
        });*/
        /*mRealm.beginTransaction();//开启事务
        User user = mRealm.createObject(User.class);
        user.setName("Gavin");
        user.setId("3");
        mRealm.commitTransaction();//提交事务*/
        //使用copyToRealmOrUpdate
        final User user = new User();
        user.setId("6");
        user.setName("Jack");
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(user);
            }
        });
    }


    private void retrieveTest1() {
        //User user = mRealm.where(User.class).findFirst();
        //RealmResults<User> userList = mRealm.where(User.class).findAll();
        RealmResults<User> userList = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .equalTo("dogs.name", "二哈")
                .findAllAsync();
        //Log.i(TAG, userList.toString());
        showUser(userList);
    }

    /**
     * 增
     */
    private void createUser() {
        //======== 用法1 ==============
        //使用createObject
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Gavin");
                //user.setAge(23);
            }
        });
        if (isTest) return;
        //插入User2
        /*mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User2 user = realm.createObject(User2.class);
                user.name = "Micheal";
                user.age = 30;
            }
        });*/

        //使用copyToRealmOrUpdate
        final User user = new User();
        //user.setId(3);
        user.setName("Jack");
        //user.setAge(28);
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(user);
            }
        });
        //创建带有多只dog的user

/*        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Gain");
                //user.setAge(23);

                Dog dog1 = realm.createObject(Dog.class);
                dog1.setAge(1);
                dog1.setName("二哈");
                //user.getDogs().add(dog1);

                Dog dog2 = realm.createObject(Dog.class);
                dog2.setAge(2);
                dog2.setName("阿拉撕家");
                //user.getDogs().add(dog2);

            }
        });*/
        //======= 用法2 ==============
        //在beginTransaction和commitTransaction中写
        mRealm.beginTransaction();
        User user2 = mRealm.createObject(User.class);
        user2.setName("Jean");
        //user2.setAge(13);
        mRealm.commitTransaction();
        mRealm.cancelTransaction();
        //使用executeTransactionAsync方法
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Eric");
                //user.setAge(33);
            }
        });
        //使用executeTransactionAsync方法，还能监听成功和失败事件

        RealmAsyncTask transaction = mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                User user = realm.createObject(User.class);
                user.setName("Eric");
                //user.setAge(33);
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


    }


    /**
     * 查
     */
    private void retrieveUser() {
        //查找所有
        RealmResults<User> userList = mRealm.where(User.class).findAll();
        showUser(userList);
        if (isTest) return;
        //查找第一个
        User user2 = mRealm.where(User.class).findFirst();
        //根据条件查找
        RealmResults<User> user3 = mRealm.where(User.class).equalTo("name", "Gavin").findAll();
        //根据dog找user
        RealmResults<User> user4 = mRealm.where(User.class).equalTo("dogs.name", "二哈").findAll();
        //先找到名叫Gavin的user，再从中找出名叫二哈的user
        RealmResults<User> user5 = mRealm.where(User.class).equalTo("name", "Gavin").findAll();
        RealmResults<User> user6 = user5.where().equalTo("dogs.name", "二哈").findAll();
        //还可以这么写
        RealmResults<User> user7 = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .equalTo("dogs.name", "二哈")
                .findAll();

        //or的使用
        RealmQuery<User> query = mRealm.where(User.class);
        query.equalTo("name", "Gavin");
        query.or().equalTo("name", "Eric");
        RealmResults<User> user8 = query.findAll();
        //还可以这样
        RealmResults<User> user9 = mRealm.where(User.class)
                .equalTo("name", "Gavin")
                .or().equalTo("name", "Eric")
                .findAll();
    }

    /**
     * 更新
     */
    private void updateUser() {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                //先查找后得到User对象
                User user = mRealm.where(User.class).findFirst();
                //user.setAge(26);
            }
        });
        retrieveUser();
    }

    /**
     * 删除
     */
    private void deleteUser() {
        //先查找到数据
        final RealmResults<User> user = mRealm.where(User.class).findAll();
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.get(0).deleteFromRealm();
            }
        });
        //或者
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                user.deleteFromRealm(0);
            }
        });

    }

    private void showUser(RealmResults<User> user) {
        Toast.makeText(this, "size : " + user.size(), Toast.LENGTH_SHORT).show();
        for (User u : user) {
            Log.i(TAG, u.toString());
        }
    }

}
