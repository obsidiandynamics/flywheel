YConf
===
Simple, elegant configuration.

# About
YConf is a mapping layer for a one-way conversion of structured documents (XML, JSON, YAML, etc.) into object graphs, optimised for configuration scenarios. It's like an ORM for documents.

YConf currently supports YAML using the [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) parser. Other document formats are on their way.

## Why not use _&lt;your favourite parser here&gt;_
Parsers such as SnakeYAML, Jackson, Gson, Genson, XStream _et al._ already support bidirectional object serialisation. And they also support custom (de)serialisers. Why the middleman?

YConf was designed to provide a parser-agnostic object mapper. With YConf you can switch from one document format to another, and your application is none the wiser. And importantly, your hand-crafted mapping code will continue to work regardless of the underlying library.

# Getting started
## Getting YConf
Gradle builds are hosted on JCenter. Just add the following snippet to your build file (replacing the version number in the snippet with the version shown on the Download badge at the top of this README).

For Maven:

```xml
<dependency>
  <groupId>com.obsidiandynamics.yconf</groupId>
  <artifactId>yconf-core</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
```

For Gradle:

```groovy
compile 'com.obsidiandynamics.yconf:yconf-core:0.1.0'
```


## Field injection
Assume the following YAML file:
```yaml
aString: hello
aNumber: 3.14
anObject:
  aBool: true
  anArray:
  - a
  - b
  - c
```

And the following Java classes:
```java
@Y
public class Top {
  @Y
  public static class Inner {
    @YInject
    boolean aBool;
    
    @YInject
    String[] anArray;
  }
  
  @YInject
  String aString = "some default";
  
  @YInject
  double aNumber;
  
  @YInject
  Inner inner;
}
```

All it takes is the following to map from the document to the object model, storing the result in a variable named `top`:
```java
final Top top = new MappingContext().fromStream(new FileInputStream("sample-basic.yaml"), Top.class);
```

The `aString` field in our example provides a default value. So if the document omits a value for `aString`, the default assignment will remain. This is really convenient when your configuration has sensible defaults. Beware of one gotcha: if the document provides a value, but that value is `null`, this is treated as the absence of a value. So if `null` happens to be a valid value in your scenario, it would also have to be the default value.



For the above example to work we've had to do a few things:

* Annotate the mapped classes with `@Y`;
* Annotate the mapped fields with `@YInject`; and
* Ensure that the classes have a public no-arg constructor.

## Constructor injection
Suppose you can't annotate fields and/or provide a no-arg constructor. Perhaps you are inheriting from a base class over which you have no control. The following is an alternative that uses constructor injection:
```java
@Y
public class Top {
  @Y
  public static class Inner {
    @YInject
    final boolean aBool;
    
    @YInject
    final String[] anArray;

    Inner(@YInject(name="aBool") boolean aBool, 
          @YInject(name="anArray") String[] anArray) {
      this.aBool = aBool;
      this.anArray = anArray;
    }
  }
  
  @YInject
  final String aString;
  
  @YInject
  final double aNumber;
  
  @YInject
  final Inner inner;

  Top(@YInject(name="aString") String aString,
      @YInject(name="aNumber") double aNumber, 
      @YInject(name="inner") Inner inner) {
    this.aString = aString;
    this.aNumber = aNumber;
    this.inner = inner;
  }
}
```

**Note:** When using constructor injection, the `name` attribute of `@YInject` is mandatory, as parameter names (unlike fields) cannot be inferred at runtime.

Constructor injection does not mandate a public no-arg constructor. In fact, it doesn't even require that your constructor is public. It does, however, require that the injected constructor is fully specified in terms of `@YInject` annotations. That is, each of the parameters must be annotated, or the constructor will not be used. At this stage, no behaviour is prescribed for partially annotated constructors or multiple constructors with `@YInject` annotations. This _may_ be supported in future versions.

## Hybrid injection
//TODO