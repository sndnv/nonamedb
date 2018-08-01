defmodule Storage.Engine do
  @callback get(Storage.Engine, String.t) :: [binary]
  @callback put(Storage.Engine, String.t, [binary]) :: :done
end
