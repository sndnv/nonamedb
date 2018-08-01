defmodule Storage.Memory.MemoryEngine do
  use GenServer
  require Logger

  @behaviour Storage.Engine

  def start_link(options) do
    GenServer.start_link(__MODULE__, :ok, options)
  end

  def get(engine, key) do
    GenServer.call(engine, {:get, key})
  end

  def put(engine, key, value) do
    GenServer.call(engine, {:put, key, value})
  end

  def init(:ok) do
    {:ok, %{}}
  end

  def handle_call(request, _from, store) do
    case request do
      {:get, key} ->
        Logger.debug(
          "[GET] Value with key [#{key}] #{if (Map.has_key?(store, key)), do: "found", else: "not found"}"
        )

        {:reply, Map.get(store, key), store}

      {:put, key, value} ->
        updatedStore = case value do
          [] ->
            Logger.debug(
              "[PUT] Removing value with key [#{key}]"
            )

            Map.delete(store, key)

          _ ->
            Logger.debug(
              "[PUT] #{if (Map.has_key?(store, key)), do: "Upadting", else: "Adding"} value with key [#{key}]"
            )

            Map.put(store, key, value)
        end

        {:reply, :done, updatedStore}
    end
  end
end
