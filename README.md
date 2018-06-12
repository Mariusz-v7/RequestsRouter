# Synapse Project

## Route arguments

### Argument without annotation
- resolved from Session
- required to be present
- no default value possible
- name is computed by calculating MD5 sum from cannonical class name

### @PathVar("name")
- resolved from URL
- required to be present
- no default value possible
- only primitive types can be parsed


