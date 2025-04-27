# control-starter

A starter for controlling the number of method calls in a given time period

[![GitHub Packages](https://img.shields.io/badge/GitHub_Packages-Download-blueviolet?logo=github)](https://github.com/drobucs/control-starter/packages)
[![License](https://img.shields.io/badge/License-MIT-brightgreen)](LICENSE)


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
        <version>0.0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

---


## Usage

### Method control

```java
import org.springframework.stereotype.Service;
import ru.drobunind.spring.starter.control.annotation.Control;

import java.util.concurrent.TimeUnit;

@Service
public class ExternalClient {
	
	@Control(value = 100, timeUnit = TimeUnit.MINUTES)
	public void doRequest() {
            // This method will be performed no more than 100 times per minute
	}
}
```

### Class control

```java
import org.springframework.stereotype.Service;
import ru.drobunind.spring.starter.control.annotation.Control;

import java.util.concurrent.TimeUnit;

@Control(value = 100, timeUnit = TimeUnit.MINUTES)
@Service
public class ExternalClient {
	public void doRequest() {
		// This method will be performed no more than 100 times per minute
	}
	
	public void doRequest2() {
		// This method will be performed no more than 100 times per minute
	}
}
```

### Exclude methods

```java
import org.springframework.stereotype.Service;
import ru.drobunind.spring.starter.control.annotation.Control;
import ru.drobunind.spring.starter.control.annotation.ControlExclude;

import java.util.concurrent.TimeUnit;

@Control(value = 100, timeUnit = TimeUnit.MINUTES)
@Service
public class ExternalClient {
	public void doRequest() {
		// This method will be performed no more than 100 times per minute
	}
	
	@ControlExclude
	public void doRequest2() {
		// This method will be executed as usual
	}
}
```

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

