defmodule Api.ServiceTest do
  @moduledoc false

  use ExUnit.Case
  use Plug.Test

  setup do
    start_supervised!({Storage.Memory.MemoryEngine, name: Store})
    :ok
  end

  @opts Api.Service.init([])

  @test_key "some_key"
  @test_value "some value"
  @updated_test_value "some updated value"

  test "rejects invalid requests" do
    conn = conn(:get, "/#{@test_key}")
    conn = Api.Service.call(conn, @opts)

    assert conn.state == :sent
    assert conn.status == 404
    assert conn.resp_body == "Not Found"
  end

  test "successfully adds and retrieves data" do
    post_conn = conn(:post, "/#{@test_key}", @test_value) |> put_req_header("content-type", "application/octet-stream")
    post_conn = Api.Service.call(post_conn, @opts)

    assert post_conn.state == :sent
    assert post_conn.status == 200
    assert post_conn.resp_body == ""

    get_conn = conn(:get, "/#{@test_key}")
    get_conn = Api.Service.call(get_conn, @opts)

    assert get_conn.state == :sent
    assert get_conn.status == 200
    assert get_conn.resp_body == @test_value
  end

  test "successfully updates and retrieves updated data" do
    post_conn = conn(:post, "/#{@test_key}", @test_value) |> put_req_header("content-type", "application/octet-stream")
    post_conn = Api.Service.call(post_conn, @opts)

    assert post_conn.state == :sent
    assert post_conn.status == 200
    assert post_conn.resp_body == ""

    get_conn_new = conn(:get, "/#{@test_key}")
    get_conn_new = Api.Service.call(get_conn_new, @opts)

    assert get_conn_new.state == :sent
    assert get_conn_new.status == 200
    assert get_conn_new.resp_body == @test_value

    put_conn = conn(:post, "/#{@test_key}", @updated_test_value) |> put_req_header("content-type", "application/octet-stream")
    put_conn = Api.Service.call(put_conn, @opts)

    assert put_conn.state == :sent
    assert put_conn.status == 200
    assert put_conn.resp_body == ""

    get_conn_updated = conn(:get, "/#{@test_key}")
    get_conn_updated = Api.Service.call(get_conn_updated, @opts)

    assert get_conn_updated.state == :sent
    assert get_conn_updated.status == 200
    assert get_conn_updated.resp_body == @updated_test_value
  end

  test "successfully deletes data and fails to retrieve it" do
    post_conn = conn(:post, "/#{@test_key}", @test_value) |> put_req_header("content-type", "application/octet-stream")
    post_conn = Api.Service.call(post_conn, @opts)

    assert post_conn.state == :sent
    assert post_conn.status == 200
    assert post_conn.resp_body == ""

    get_conn_new = conn(:get, "/#{@test_key}")
    get_conn_new = Api.Service.call(get_conn_new, @opts)

    assert get_conn_new.state == :sent
    assert get_conn_new.status == 200
    assert get_conn_new.resp_body == @test_value

    delete_conn = conn(:delete, "/#{@test_key}")
    delete_conn = Api.Service.call(delete_conn, @opts)

    assert delete_conn.state == :sent
    assert delete_conn.status == 200
    assert delete_conn.resp_body == ""

    get_conn_deleted = conn(:get, "/#{@test_key}")
    get_conn_deleted = Api.Service.call(get_conn_deleted, @opts)

    assert get_conn_deleted.state == :sent
    assert get_conn_deleted.status == 404
    assert get_conn_deleted.resp_body == "Not Found"
  end
end
