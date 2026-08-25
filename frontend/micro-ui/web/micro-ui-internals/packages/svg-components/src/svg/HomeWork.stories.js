import React from "react";
import { HomeWork } from "./HomeWork";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "HomeWork",
  component: HomeWork,
};

export const Default = () => <HomeWork />;
export const Fill = () => <HomeWork fill="blue" />;
export const Size = () => <HomeWork height="50" width="50" />;
export const CustomStyle = () => <HomeWork style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <HomeWork className="custom-class" />;

export const Clickable = () => <HomeWork onClick={()=>console.log("clicked")} />;

const Template = (args) => <HomeWork {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
