YConf
===
Simple, elegant configuration.

# About
YConf is a mapping layer for a one-way conversion of structured documents (XML, JSON, YAML, etc.) into object graphs, specifically optimised for configuration scenarios. It is not a general-purpose object serialisation framework; instead, it's like an ORM for configuration artefacts.

YConf currently supports YAML using the [SnakeYAML](https://bitbucket.org/asomov/snakeyaml) parser. Other document formats are on their way.

## Why not use _&lt;insert your favourite parser here&gt;_
Parsers such as SnakeYAML, Jackson, Gson, Genson, XStream _et al._ already support bidirectional object serialisation. And they also support custom (de)serialisers. So why the middleman?

YConf was designed to provide a parser-agnostic object mapper. With YConf you can switch from one document format to another, and your application is none the wiser. And importantly, your hand-crafted mapping code will continue to work regardless of the underlying library.

YConf standardises type mappings. Your team might support multiple projects with varying configuration needs - different formats, parsers and mappings. Rather than learning the intricacies of every parser, YConf provides a uniform language that works across parsers.

# Getting Started
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

The `@YInject` annotation has two optional fields:

* `name` - The name of the attribute in the document that will be mapped to the annotated field or parameter. If omitted, the name will be inferred from the annotated field. If the annotation is applied to a [constructor parameter](#user-content-constructor-injection), the name must be set explicitly.
* `type` - The `Class` type of the mapped object. If omitted, the type will be inferred from the annotated field or parameter.

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
This is basically constructor injection, topped off with field injection - for any annotated fields that weren't set by the constructor. The latter takes place automatically, immediately after object instantiation.

# Custom Mappings
The earlier examples assume that the configuration corresponds, more or less, to the resulting object graph. It's also assumed that you have some control over the underlying classes, at least to add the appropriate annotations. Sometimes this isn't the case.

## Type mapping 101
We need to dissect some of the underlying mechanisms before we go any further. At the heart of YConf there are three main classes:

* `MappingContext` - Holds contextual data about the current mapping session, as well as settings - a registry of type mappers and DOM transforms. When you need to change YConf's behaviour, this is the class you use.
* `YObject` - A wrapper around a section of the underlying document object model (DOM) which, in turn, is the raw output of the parser. If you can visualise the entire DOM as a tree that will be mapped to the root of your resulting object graph, a `YObject` will house a subtree that corresponds to the current point in the graph where the mapper is currently operating.
* `TypeMapper` - An interface specifying how a `YObject` is mapped to an output object. This is YConf's main extension point - allowing you to specify custom mapping behaviour.

## Built-in mappers
### `RuntimeMapper`
This mapper is by default applied to everything of type `Object`, as well as to any types that are not explicitly added to the type mapper registry. In other words, if you are trying to map to an output of an unknown type, a `RuntimeMapper` is what gets used. So when wouldn't you know the target type?

One word: _polymorphism_. If the target or parameter is a subclass (or sub-interface) of the _concrete_ object, then the target type is virtually useless to the mapper. What it needs is the concrete type, and this can only come from the configuration document. The preferred way to specify the concrete type is to state its fully-qualified class name in a special `type` attribute, as illustrated in the example below.

```yaml
animals:
- type: com.acme.Dog
  name: Dingo
  breed: Labrador
- type: com.acme.Bird
  name: Olly
  wingspan: 13.47
```

The `animals` field can be an `Object[]` or an `Animal[]` (assuming `Dog` and `Bird` extend `Animal`). It really doesn't matter, as YConf will always consult the `type` attribute when no mapper is defined for the target (base) type. If, for some reason, you can't use the name `type` in your configuration (perhaps `type` is already taken to mean something else), the name attribute can be overridden as follows:

```java
new MappingContext()
.withMapper(Object.class, new RuntimeMapper().withTypeAttribute("anotherAttribute"))
.fromStream(...);
```

If all your types come from the same base package, you can do one better. The `RuntimeMapper` has a `withTypeFormatter()` method, allowing you to alter the value of the type attribute. This can be used to perform any manipulation on the type name; for example, to prefix the type with a base package:

```java
new MappingContext()
.withMapper(Object.class, new RuntimeMapper().withTypeAttribute("_type").withTypeFormatter("com.acme."::concat))
.fromStream(...);
```

The above setting can now be used with the following document:

```yaml
animals:
- _type: Dog
  name: Dingo
  breed: Labrador
- _type: Bird
  name: Olly
  wingspan: 13.47
```


### `ReflectiveMapper`
This mapper was used in our initial examples, to reflectively populate with fields and parameters annotated with `@YInject`. It is also the default mapper used where an class is annotated with `@Y`, where no explicit `TypeMapper` class is specified.

### `CoercingMapper`
Coercing is the process of 'forcing' one type to another (not to be confused with casting), and is normally used with scalar values. A `CoercingMapper` performs an optional conversion by first comparing the type of the original value in the DOM with the target type, passing the value unchanged if the target type is assignable from the original. Otherwise, if the types are incompatible, coercion will occur by first reducing the original to a `String` (by calling its `toString()` method) and then invoking a supplied `CoercingMapper.StringConverter` implementation, taking in a `String` value and outputting a subclass of the target type. (Note: `null` objects are always passed through uncoerced.)

Coercion is typically used where the original type is somewhat similar to the target type, but cannot be converted through a conventional cast or a(n) (un)boxing operation. For example, a string literal containing a sequence of digits appears to be a number, but isn't. In this case, coercion will run the original string through `Long::parseLong` (or another parser, as appropriate) to get the desired outcome.

Type boxing is another area where YConf uses coercion. For example, an object's properties are typically represented using a `Map<String, Object>` in the DOM. For number types, document parsers typically output the wider of the possible forms (e.g. `long` in place of `int`, `double` over `float`, etc.). Because values in a map must be of a reference type, a narrowing type cast cannot be used if the target (primitive or reference) type is narrower than the original reference type.

Finally, we can use coercion to translate strings to a more complex type. For example, a string containing a fully qualified class name can be coerced to a `Class` type. You can easily add your own coercions, by supplying a lambda that takes in a `String` and outputs the target type. The example below demonstrates this technique using the `URL` class (supplying the `URL(String)` constructor).

```java
new MappingContext().withMapper(URL.class, new CoercingMapper(URL.class, URL::new))...
```

## Writing your own
Now we get to the crux of it. You need a way to create an arbitrary object graph that bears little resemblance to the underlying configuration file.

Suppose we have the following file, containing a list of named server endpoints:
```yaml
- name: Health check
  protocol: http
  host: localhost
  port: 8080
  path: /health

- name: Message broker
  protocol: ws
  host: broker.acme.com
  path: /broker

- name: Service discovery
  protocol: https
  host: sd.acme.com
```

We'd like to translate this to a class that contains a map of endpoint names to their `URI`s:
```java
public final class WebConfig {
  final Map<String, URI> servers;
}
```

Furthermore, we'd like to ensure that the endpoint names are unique, throwing an error if a duplicate name is encountered. We also want the `port` and `path` fields to be optional.

Start by implementing a `TypeMapper` for the root object.
```java
public final class Mapper implements TypeMapper {
  @Override public Object map(YObject y, Class<?> type) {
    final Map<String, URI> servers = new HashMap<>();
    for (YObject server : y.asList()) {
      final String name = server.mapAttribute("name", String.class);
      if (servers.containsKey(name)) {
        throw new MappingException("Duplicate server name " + name);
      }
      
      final String protocol = server.mapAttribute("protocol", String.class);
      final String host = server.mapAttribute("host", String.class);
      final Integer port = server.mapAttribute("port", Integer.class);
      final String path = server.mapAttribute("path", String.class);
      final URI uri;
      try {
        uri = new URI(protocol, null, host, port != null ? port : -1, path, null, null);
      } catch (URISyntaxException e) {
        throw new MappingException("Error parsing URI", e);
      }
      servers.put(name, uri);
    }
    return new WebConfig(servers);
  }
}
```

This is a simple class with one _functional_ method - taking in a DOM fragment and the desired type (which we already know to be `WebConfig`) and outputting a `WebConfig` instance. But before going further, let's pause for a minute to consider how YConf actually works.

Behind the scenes, all structured document parsers really deal with just three kinds of elements - **scalars**, **lists** and **maps**. (And where this mightn't be the case, it's relatively straightforward to map the parser's output to a scalar, list or a map.) The scalar is usually either a primitive type or its boxed equivalent, and sometimes simple types such as a `Date` might also be supported natively by the parser. A list is typically an `ArrayList<?>`. A map tends to be a `LinkedHashMap<?, ?>` (to preserve insertion order). 

Part of YConf's value-add is the assurance that the underlying DOM is either a scalar, a `List<YObject>`, or a `Map<String, YObject>` - irrespective of the native data types that the backing parser might emit. As far as scalars go, the value will be of the widest type and _may_ also come pre-converted, if the parser was under instruction to do so.

Now that we know the fundamentals, let's take another look at the original document. To us it's now just a list of maps.

The configuration is an array at the top level, so our mapper calls the `asList()` method of the given DOM, which returns a `List<YObject>` - listing each item in the array. Each of the elements is a `Map`, so we use the `mapAttribute()` method to convert the value of an attribute to a specific target type.

Notice the use of recursion? The `mapAttribute()` method doesn't simply return the raw value; it actually uses the underlying `MappingContext` (embedded within the `YObject` instance) to initiate a deeper query into the DOM which, in turn, will invoke another mapper as required. Also, `mapAttribute("name", String.class)` is just another way of saying `asMap().get("name").map(String.class)`.

After extracting the name, we check that our staging `servers` map doesn't already contain an identically-named entry. If it does, we'll throw a `MappingException` - an unchecked exception that bubble up to our ultimate caller. The rest of the code is fairly trivial; we map the remaining attributes onto local variables, which are then used to construct a `URI`. The requirement to support optional `port` and `path` fields is accommodated by the use of reference types (`Integer` in place of an `int`); the mapper will return `null` if the requested attribute value isn't present in the document, or is explicitly set to `null`.

**Note:** You might be wondering - why does the `map()` method need the `type` parameter, given that our mapper implementation already knows which type it should be dealing with? What else could it be? The reason is that although our custom mapper is very specific, there are other mappers (such as the `CoercingMapper`) that are generic - designed to deal with a variety of types. So knowing the type at runtime may occasionally be necessary.

The next step is to register our mapper implementation with the `MappingContext`. There are two ways this can be done: using the `@Y` annotation, or by registering directly with the `MappingContext` instance.

The example below shows the annotation approach.

```java
@Y(WebConfig.Mapper.class)
public final class WebConfig {
  public static final class Mapper implements TypeMapper {
    @Override public Object map(YObject y, Class<?> type) {
      // the mapper implementation, omitted for brevity
    }
  }
  
  final Map<String, URI> servers;

  WebConfig(Map<String, URI> servers) {
    this.servers = servers;
  }
}
```

We've embedded the mapper into the config class for convenience, and have referenced it from `@Y`. Simple.

**Note:** Classes referenced from `@Y` must be bean-instantiable. That is, they must be public, have a public no-arg constructor, and must not have an encapsulating instance. The latter is easy to overlook when using a nested class; make sure you're declaring your mapper with the `static` modifier if nesting within another type.

Alternatively, if you don't (or can't) add an annotation to your class, the following snippet registers the type mapper directly with the context.
```java
new MappingContext().withMapper(WebConfig.class, new WebConfig.Mapper())...
```