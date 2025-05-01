# control-starter

A spring starter for controlling the number of method calls in a given time period

[![Build Status](https://github.com/drobucs/control-starter/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/drobucs/control-starter/actions)

[![License](https://img.shields.io/badge/License-MIT-brightgreen)](LICENSE)

![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/drobucs/control-starter?)

---

## Installation

### Maven

```xml

<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/drobucs/control-starter</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>ru.drobunind</groupId>
    <artifactId>control-starter</artifactId>
    <version>${control.starter.version}</version>
</dependency>
</dependencies>
```

---

## Usage

Currently, the `@Control` annotation controls only the interface methods that the class implements.

```java
public interface Client {
	void doRequest();

	void doRequest2();
}
```

### Method control

```java
@Component
public class ExternalClient implements Client {

	@Control(value = 100, timeUnit = TimeUnit.MINUTES)
	@Override
	public void doRequest() {
		// This method will be performed no more than 100 times per minute
	}

	@Override
	public void doRequest2() {
		// This method will be executed as usual
	}
}
```

### Class control

```java
@Control(value = 100, timeUnit = TimeUnit.MINUTES)
@Component
public class ExternalClient implements Client {

	@Override
	public void doRequest() {
		// This method will be performed no more than 100 times per minute
	}

	@Override
	public void doRequest2() {
		// This method will be performed no more than 100 times per minute
	}
}
```

### Exclude methods

```java
@Control(value = 100, timeUnit = TimeUnit.MINUTES)
@Component
public class ExternalClient implements Client {

	@Override
	public void doRequest() {
		// This method will be performed no more than 100 times per minute
	}

	@ControlExclude
	@Override
	public void doRequest2() {
		// This method will be executed as usual
	}
}
```
You can see more examples in the [tests](./src/test/java/ru/drobunind/spring/starter/cases).

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

