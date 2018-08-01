defmodule Storage.Memory.MemoryEngineTest do
  @moduledoc false

  alias Storage.Memory.MemoryEngine, as: MemoryEngine
  use ExUnit.Case, async: true
  doctest Storage.Memory.MemoryEngine

  @test_key "some_key"
  @test_value "some value" |> :binary.bin_to_list()
  @updated_test_value "some updated value" |> :binary.bin_to_list()

  setup do
    engine = start_supervised!(MemoryEngine)
    %{engine: engine}
  end

  test "fails to retrieve missing data", %{engine: engine} do
    assert MemoryEngine.get(engine, "some") == nil
  end

  test "successfully adds and retrieves data", %{engine: engine} do
    assert MemoryEngine.put(engine, @test_key, @test_value) == :done
    assert MemoryEngine.get(engine, @test_key) == @test_value
  end

  test "successfully updates and retrieves updated data", %{engine: engine} do
    assert MemoryEngine.put(engine, @test_key, @test_value) == :done
    assert MemoryEngine.get(engine, @test_key) == @test_value
    assert MemoryEngine.put(engine, @test_key, @updated_test_value) == :done
    assert MemoryEngine.get(engine, @test_key) == @updated_test_value
  end

  test "successfully removes data and fails to retrieve it", %{engine: engine} do
    assert MemoryEngine.put(engine, @test_key, @test_value) == :done
    assert MemoryEngine.get(engine, @test_key) == @test_value
    assert MemoryEngine.put(engine, @test_key, "" |> :binary.bin_to_list()) == :done
    assert MemoryEngine.get(engine, @test_key) == nil
  end
end
