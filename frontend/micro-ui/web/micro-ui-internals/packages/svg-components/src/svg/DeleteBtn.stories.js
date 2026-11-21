import React from "react";
import { DeleteBtn } from "./DeleteBtn";

export default {
  title: "DeleteBtn",
  component: DeleteBtn,
  argTypes: {
    fill: { control: "color" },
    height: { control: "number" },
    width: { control: "number" },
    style: { control: "object" },
    className: {
      options: ["custom-class"],
      control: { type: "select" },
    },
    onClick: { action: "clicked" },
  },
};

export const Default = () => <DeleteBtn />;
export const Fill = () => <DeleteBtn fill="blue" />;
export const Size = () => <DeleteBtn height={50} width={50} />;
export const CustomStyle = () => <DeleteBtn style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <DeleteBtn className="custom-class" />;

export const Clickable = () => <DeleteBtn onClick={() => console.log("clicked")} />;

const Template = (args) => <DeleteBtn {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  fill: "green",
  height: 30,
  width: 30,
  style: { border: "3px solid green" },
  className: "custom-class",
};
