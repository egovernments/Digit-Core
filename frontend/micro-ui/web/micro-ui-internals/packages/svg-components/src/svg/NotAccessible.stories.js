import React from "react";
import { NotAccessible } from "./NotAccessible";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NotAccessible",
  component: NotAccessible,
};

export const Default = () => <NotAccessible />;
export const Fill = () => <NotAccessible fill="blue" />;
export const Size = () => <NotAccessible height="50" width="50" />;
export const CustomStyle = () => <NotAccessible style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NotAccessible className="custom-class" />;

export const Clickable = () => <NotAccessible onClick={()=>console.log("clicked")} />;

const Template = (args) => <NotAccessible {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
