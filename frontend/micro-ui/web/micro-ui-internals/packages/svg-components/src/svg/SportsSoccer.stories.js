import React from "react";
import { SportsSoccer } from "./SportsSoccer";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SportsSoccer",
  component: SportsSoccer,
};

export const Default = () => <SportsSoccer />;
export const Fill = () => <SportsSoccer fill="blue" />;
export const Size = () => <SportsSoccer height="50" width="50" />;
export const CustomStyle = () => <SportsSoccer style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SportsSoccer className="custom-class" />;

export const Clickable = () => <SportsSoccer onClick={()=>console.log("clicked")} />;

const Template = (args) => <SportsSoccer {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
