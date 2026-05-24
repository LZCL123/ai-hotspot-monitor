import OpenCC from 'opencc-js'

const converter = OpenCC.Converter({ from: 't', to: 'cn' })
const skipKeys = new Set(['url', 'token'])

export function toSimplified(value: string) {
  return converter(value)
}

export function simplifyApiData<T>(value: T, key?: string): T {
  if (typeof value === 'string') {
    return (skipKeys.has(key || '') ? value : toSimplified(value)) as T
  }
  if (Array.isArray(value)) {
    return value.map((item) => simplifyApiData(item)) as T
  }
  if (value && typeof value === 'object') {
    const entries = Object.entries(value).map(([entryKey, entryValue]) => [
      entryKey,
      simplifyApiData(entryValue, entryKey)
    ])
    return Object.fromEntries(entries) as T
  }
  return value
}
