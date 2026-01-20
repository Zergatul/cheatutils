# Website components

## Explanation:-

- `v-model="config.VariableName"` This defines the variable to be modified.

- `@change="update()"` This defines the function to be run on value change

### [Website uses Vue components](https://vuejs.org/guide/introduction.html)

## Component Examples:-

### Switch

```html
<switch-checkbox v-model="config.enabled" @change="update()">Enabled</switch-checkbox>
```

#### boolean output

### Text Box

```html
<input type="text" class="w3" v-model="config.speedThreshold" @change="update()">
```

#### Parses output to the variable type
