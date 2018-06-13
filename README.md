# Synapse Project

## Paths
Paths provided in `@Controller` and `@Route` are joined.
Paths are always normalized to start with a slash and end without it. Multiple repeated slashes are also removed.

E.g.
`@Controller("path")` and `@Route("route")` will be resolved to `/path/route`

`@Controller` and `@Route` will be resolved to `/`.

`@Controller("/")` and `@Route("/")` will be also resolved to `/`.

And finally `@Controller("/path/")` and `@Route("/route/")` will be resolved to `/path/route`.

## Route arguments

1. Argument without annotation
- resolved from `Session`
- required to be present
- no default value possible
- `name` is computed by calculating MD5 sum from cannonical class name

2. `@PathVar("name")`
- resolved from URL
- required to be present
- no default value possible
- `name` cannot be empty
- `name` can be consisted only from `[-A-Za-z0-9_]`
- only primitive types can be parsed

3. `@Arg(value = "name", defaultValue = "null", required = true)`
- `name` has to be provided
- `name` cannot be empty
- `default value` resolves to `null` by default
- `default value` is parsed by Spring's expression parser

4. `@SessionVar(value = "", defaultValue = "null", required = true)`
- `name` is optional
- `name` is computed by calculating MD5 sum from cannonical class name if it isn't provided
- `default value` resolves to `null` by default
- `default value` is parsed by Spring's expression parser
